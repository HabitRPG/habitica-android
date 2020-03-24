package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.amplitude.api.Amplitude
import com.google.gson.JsonSyntaxException
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.ApiService
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.Server
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.auth.UserAuth
import com.habitrpg.android.habitica.models.auth.UserAuthResponse
import com.habitrpg.android.habitica.models.auth.UserAuthSocial
import com.habitrpg.android.habitica.models.auth.UserAuthSocialTokens
import com.habitrpg.android.habitica.models.responses.*
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.*
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.shared.habitica.data.ApiRequest
import com.habitrpg.shared.habitica.data.OfflineClient
import com.habitrpg.shared.habitica.interactors.ScoreTaskLocallyInteractor
import com.habitrpg.shared.habitica.interactors.UserLocalInteractor
import com.habitrpg.shared.habitica.models.Tag
import com.habitrpg.shared.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.models.inventory.Quest
import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.user.Items
import com.habitrpg.shared.habitica.models.user.Stats
import com.habitrpg.shared.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException


class ApiClientImpl//private OnHabitsAPIResult mResultListener;
//private HostConfig mConfig;
(private val gsonConverter: GsonConverterFactory, override val hostConfig: HostConfig, private val crashlyticsProxy: CrashlyticsProxy, private val notificationsManager: NotificationsManager, private val offlineClient: OfflineClient, private val context: Context) : Consumer<Throwable>, ApiClient {
    private lateinit var retrofitAdapter: Retrofit

    // I think we don't need the ApiClientImpl anymore we could just use ApiService
    private lateinit var apiService: ApiService

    private val apiOfflineErrorHandlerTransformer = { offlineCallback: () -> Any? ->
        FlowableTransformer<Any, Any> { observable ->
            observable
                    .onErrorReturn { error ->
                        this.accept(error)
                        if (error is SocketException || error is SSLException) {
                            offlineCallback()
                        } else {
                            null
                        }
                    }
        }
    }

    private val apiOnlineErrorHandlerTransformer = FlowableTransformer<Any, Any> { observable ->
        observable.doOnError(this)
    }

    private val apiCallTransformer = FlowableTransformer<HabitResponse<Any>, Any> { observable ->
        observable
                .filter { it.data != null }
                .map { habitResponse ->
                    if (habitResponse.notifications != null) {
                        notificationsManager.setNotifications(habitResponse.notifications)
                    }
                    habitResponse.data
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this)
    }
    private var languageCode: String? = null
    private var lastAPICallURL: String? = null

    init {
        this.notificationsManager.setApiClient(this)

        HabiticaBaseApplication.userComponent?.inject(this)
        crashlyticsProxy.setUserIdentifier(this.hostConfig.userID)
        crashlyticsProxy.setUserName(this.hostConfig.userID)
        Amplitude.getInstance().userId = this.hostConfig.userID
        buildRetrofit()
    }

    private fun buildRetrofit() {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        }

        val userAgent = System.getProperty("http.agent")

        val calendar = GregorianCalendar()
        val timeZone = calendar.timeZone
        val timezoneOffset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.timeInMillis).toLong(), TimeUnit.MILLISECONDS)

        val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addNetworkInterceptor { chain ->
                    val original = chain.request()
                    var builder: Request.Builder = original.newBuilder()
                    if (this.hostConfig.hasAuthentication()) {
                        builder = builder
                                .header("x-api-key", this.hostConfig.apiKey)
                                .header("x-api-user", this.hostConfig.userID)
                    }
                    builder = builder.header("x-client", "habitica-android")
                            .header("x-user-timezoneOffset", timezoneOffset.toString())
                    if (userAgent != null) {
                        builder = builder.header("user-agent", userAgent)
                    }
                    if (BuildConfig.STAGING_KEY.isNotEmpty()) {
                        builder = builder.header("Authorization", "Basic " + BuildConfig.STAGING_KEY)
                    }
                    val request = builder.method(original.method(), original.body())
                            .build()
                    lastAPICallURL = original.url().toString()
                    chain.proceed(request)
                }
                .readTimeout(2400, TimeUnit.SECONDS)
                .build()

        val server = Server(this.hostConfig.address)

        retrofitAdapter = Retrofit.Builder()
                .client(client)
                .baseUrl(server.toString())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build()

        this.apiService = retrofitAdapter.create(ApiService::class.java)
    }

    override fun updateServerUrl(newAddress: String?) {
        if (newAddress != null) {
            hostConfig.address = newAddress
            buildRetrofit()
        }
    }

    override fun registerUser(username: String, email: String, password: String, confirmPassword: String): Flowable<UserAuthResponse> {
        val auth = UserAuth()
        auth.username = username
        auth.password = password
        auth.confirmPassword = confirmPassword
        auth.email = email
        return this.apiService.registerUser(auth).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler()).compose(configureApiOnlineErrorHandler())
    }

    override fun connectUser(username: String, password: String): Flowable<UserAuthResponse> {
        val auth = UserAuth()
        auth.username = username
        auth.password = password
        return this.apiService.connectLocal(auth).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun connectSocial(network: String, userId: String, accessToken: String): Flowable<UserAuthResponse> {
        val auth = UserAuthSocial()
        auth.network = network
        val authResponse = UserAuthSocialTokens()
        authResponse.client_id = userId
        authResponse.access_token = accessToken
        auth.authResponse = authResponse

        return this.apiService.connectSocial(auth).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun loginApple(authToken: String): Flowable<UserAuthResponse> {
        return apiService.loginApple(mapOf(Pair("code", authToken))).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun accept(throwable: Throwable) {
        val throwableClass = throwable.javaClass
        if (SocketTimeoutException::class.java.isAssignableFrom(throwableClass)) {
            return
        }
        @Suppress("DEPRECATION")
        if (SocketException::class.java.isAssignableFrom(throwableClass) || SSLException::class.java.isAssignableFrom(throwableClass)) {
            this.showConnectionProblemDialog(R.string.internal_error_api)
        } else if (throwableClass == SocketTimeoutException::class.java || UnknownHostException::class.java == throwableClass) {
            this.showConnectionProblemDialog(R.string.network_error_no_network_body)
        } else if (throwableClass == retrofit2.adapter.rxjava2.HttpException::class.java) {
            val error = throwable as HttpException
            val res = getErrorResponse(error)
            val status = error.code()

            if (status == 404 || error.response()?.raw()?.request()?.url()?.toString()?.endsWith("/user/push-devices") == true) {
                //workaround for an error that sometimes displays that the user already has this push device
                return
            }

            if (status in 400..499) {
                if (res.displayMessage.isNotEmpty()) {
                    showConnectionProblemDialog("", res.displayMessage)
                } else if (status == 401) {
                    showConnectionProblemDialog(R.string.authentication_error_title, R.string.authentication_error_body)
                }
            } else if (status in 500..599) {
                this.showConnectionProblemDialog(R.string.internal_error_api)
            } else {
                showConnectionProblemDialog(R.string.internal_error_api)
            }
        } else if (JsonSyntaxException::class.java.isAssignableFrom(throwableClass)) {
            crashlyticsProxy.log("Json Error: " + lastAPICallURL + ",  " + throwable.message)
        } else {
            crashlyticsProxy.logException(throwable)
        }
    }

    override fun getErrorResponse(throwable: HttpException): ErrorResponse {
        val errorResponse = throwable.response()?.errorBody() ?: return ErrorResponse()
        val errorConverter = gsonConverter
                .responseBodyConverter(ErrorResponse::class.java, arrayOfNulls(0), retrofitAdapter)
        return try {
            errorConverter?.convert(errorResponse) as ErrorResponse
        } catch (e: IOException) {
            crashlyticsProxy.log("Json Error: " + lastAPICallURL + ",  " + e.message)
            ErrorResponse()
        }

    }

    override fun retrieveUser(withTasks: Boolean): Flowable<User> {
        var userObservable = this.user

        if (withTasks) {
            val tasksObservable = this.tasks

            userObservable = Flowable.zip(userObservable, tasksObservable,
                    BiFunction { habitRPGUser, tasks ->
                        habitRPGUser.tasks = tasks
                        habitRPGUser
                    })
        }
        return userObservable
    }

    override fun retrieveInboxMessages(uuid: String, page: Int): Flowable<List<ChatMessage>> {
        return apiService.getInboxMessages(uuid, page).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun retrieveInboxConversations(): Flowable<List<InboxConversation>> {
        return apiService.getInboxConversations().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun hasAuthenticationKeys(): Boolean {
        return this.hostConfig.userID.isNotEmpty() && hostConfig.apiKey.isNotEmpty()
    }

    private fun showConnectionProblemDialog(resourceMessageString: Int) {
        showConnectionProblemDialog(null, context.getString(resourceMessageString))
    }

    private fun showConnectionProblemDialog(resourceTitleString: Int, resourceMessageString: Int) {
        showConnectionProblemDialog(context.getString(resourceTitleString), context.getString(resourceMessageString))
    }

    private fun showConnectionProblemDialog(resourceTitleString: String?, resourceMessageString: String) {
        val event = ShowConnectionProblemEvent(resourceTitleString, resourceMessageString)
        EventBus.getDefault().post(event)
    }

    /*
     This function is used with Observer.compose to reuse transformers across the application.
     See here for more info: http://blog.danlew.net/2015/03/02/dont-break-the-chain/
     */
    override fun <T> configureApiCallObserver(): FlowableTransformer<HabitResponse<T>, T> {
        return apiCallTransformer as FlowableTransformer<HabitResponse<T>, T>
    }

    override fun <T> configureApiOnlineErrorHandler(): FlowableTransformer<T, T> {
        return apiOnlineErrorHandlerTransformer as FlowableTransformer<T, T>
    }

    override fun <T> configureApiOfflineErrorHandler(offlineCallback: (() -> T?)): FlowableTransformer<T, T> {
        return apiOfflineErrorHandlerTransformer(offlineCallback) as FlowableTransformer<T, T>
    }

    override fun updateAuthenticationCredentials(userID: String?, apiToken: String?) {
        this.hostConfig.userID = userID ?: ""
        this.hostConfig.apiKey = apiToken ?: ""
        crashlyticsProxy.setUserIdentifier(this.hostConfig.userID)
        crashlyticsProxy.setUserName(this.hostConfig.userID)
        Amplitude.getInstance().userId = this.hostConfig.userID
    }

    override fun setLanguageCode(languageCode: String) {
        this.languageCode = languageCode
    }

    override val status: Flowable<Status>
        get() = apiService.status.compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())

    override fun getContent(language: String): Flowable<ContentResult> {
        return apiService.getContent(language).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override val user: Flowable<User>
        get() = apiService.user.compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())

    override fun updateUser(updateDictionary: Map<String, Any>): Flowable<User> {
        return apiService.updateUser(updateDictionary).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun registrationLanguage(registrationLanguage: String): Flowable<User> {
        return apiService.registrationLanguage(registrationLanguage).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun retrieveInAppRewards(): Flowable<List<ShopItem>> {
        return apiService.retrieveInAppRewards().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun retrieveOldGear(): Flowable<List<ShopItem>> {
        return apiService.retrieveOldGearRewards().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun equipItem(type: String, itemKey: String): Flowable<Items> {
        return apiService.equipItem(type, itemKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun buyItem(itemKey: String): Flowable<BuyResponse> {
        return apiService.buyItem(itemKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun purchaseItem(type: String, itemKey: String): Flowable<Any> {
        return apiService.purchaseItem(type, itemKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun validateSubscription(request: SubscriptionValidationRequest): Flowable<Any> {
        return apiService.validateSubscription(request).map { habitResponse ->
            if (habitResponse.notifications != null) {
                notificationsManager.setNotifications(habitResponse.notifications)
            }
            habitResponse.getData()
        }
    }

    override fun validateNoRenewSubscription(request: PurchaseValidationRequest): Flowable<Any> {
        return apiService.validateNoRenewSubscription(request).map { habitResponse ->
            if (habitResponse.notifications != null) {
                notificationsManager.setNotifications(habitResponse.notifications)
            }
            habitResponse.getData()
        }
    }

    override fun cancelSubscription(): Flowable<Any> {
        return apiService.cancelSubscription().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun purchaseHourglassItem(type: String, itemKey: String): Flowable<Any> {
        return apiService.purchaseHourglassItem(type, itemKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun purchaseMysterySet(itemKey: String): Flowable<Any> {
        return apiService.purchaseMysterySet(itemKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun purchaseQuest(key: String): Flowable<Any> {
        return apiService.purchaseQuest(key).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun sellItem(itemType: String, itemKey: String): Flowable<User> {
        return apiService.sellItem(itemType, itemKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun feedPet(petKey: String, foodKey: String): Flowable<FeedResponse> {
        return apiService.feedPet(petKey, foodKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun hatchPet(eggKey: String, hatchingPotionKey: String): Flowable<Items> {
        return apiService.hatchPet(eggKey, hatchingPotionKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override val tasks: Flowable<TaskList>
        get() = apiService.tasks.compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())

    override fun getTasks(type: String): Flowable<TaskList> {
        return apiService.getTasks(type).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }


    override fun getTasks(type: String, dueDate: String): Flowable<TaskList> {
        return apiService.getTasks(type, dueDate).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }


    override fun unlockPath(path: String): Flowable<UnlockResponse> {
        return apiService.unlockPath(path).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getTask(id: String): Flowable<Task> {
        return apiService.getTask(id).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun offlinePostTaskDirection(user: User, task: Task, direction: TaskDirection): Flowable<TaskDirectionData> {
        val taskId = task.id
        return if (taskId == null) {
            Flowable.empty()
        } else {
            apiService.postTaskDirection(taskId, direction.text)
                    .compose(configureApiCallObserver())
                    .compose(configureApiOfflineErrorHandler<TaskDirectionData> {
                        offlineClient.addPendingRequest(ApiRequest {
                            apiService.postTaskDirection(taskId, direction.text)
                        })
                        ScoreTaskLocallyInteractor.score(user, task, direction)
                    })
        }
    }

    override fun postTaskDirection(id: String, direction: String): Flowable<TaskDirectionData> {
        return apiService.postTaskDirection(id, direction).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun postTaskNewPosition(id: String, position: Int): Flowable<List<String>> {
        return apiService.postTaskNewPosition(id, position).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun scoreChecklistItem(taskId: String, itemId: String): Flowable<Task> {
        return apiService.scoreChecklistItem(taskId, itemId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun createTask(item: Task): Flowable<Task> {
        return apiService.createTask(item).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun createTasks(tasks: List<Task>): Flowable<List<Task>> {
        return apiService.createTasks(tasks).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateTask(id: String, item: Task): Flowable<Task> {
        return apiService.updateTask(id, item).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deleteTask(id: String): Flowable<Void> {
        return apiService.deleteTask(id).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun createTag(tag: Tag): Flowable<Tag> {
        return apiService.createTag(tag).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateTag(id: String, tag: Tag): Flowable<Tag> {
        return apiService.updateTag(id, tag).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deleteTag(id: String): Flowable<Void> {
        return apiService.deleteTag(id).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun sleep(): Flowable<Boolean> {
        return apiService.sleep().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun revive(user: User): Flowable<User> {
        return apiService.revive().compose(configureApiCallObserver()).compose(configureApiOfflineErrorHandler {
            this.offlineClient.addPendingRequest(ApiRequest { apiService.revive() })
            UserLocalInteractor.revive(user)
        })
    }

    override fun useSkill(skillName: String, targetType: String, targetId: String): Flowable<SkillResponse> {
        return apiService.useSkill(skillName, targetType, targetId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun useSkill(skillName: String, targetType: String): Flowable<SkillResponse> {
        return apiService.useSkill(skillName, targetType).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun changeClass(): Flowable<User> {
        return apiService.changeClass().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun changeClass(className: String): Flowable<User> {
        return apiService.changeClass(className).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun disableClasses(): Flowable<User> {
        return apiService.disableClasses().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun markPrivateMessagesRead(): Flowable<Void> {
        //This is necessary, because the API call returns weird data.
        return apiService.markPrivateMessagesRead()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this)
    }

    override fun listGroups(type: String): Flowable<List<Group>> {
        return apiService.listGroups(type).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getGroup(groupId: String): Flowable<Group> {
        return apiService.getGroup(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun createGroup(group: Group): Flowable<Group> {
        return apiService.createGroup(group).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateGroup(id: String, item: Group): Flowable<Group> {
        return apiService.updateGroup(id, item).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun removeMemberFromGroup(groupID: String, userID: String): Flowable<Void> {
        return apiService.removeMemberFromGroup(groupID, userID).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun listGroupChat(groupId: String): Flowable<List<ChatMessage>> {
        return apiService.listGroupChat(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun joinGroup(groupId: String): Flowable<Group> {
        return apiService.joinGroup(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun leaveGroup(groupId: String, keepChallenges: String): Flowable<Void> {
        return apiService.leaveGroup(groupId, keepChallenges).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun postGroupChat(groupId: String, message: Map<String, String>): Flowable<PostChatMessageResult> {
        return apiService.postGroupChat(groupId, message).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deleteMessage(groupId: String, messageId: String): Flowable<Void> {
        return apiService.deleteMessage(groupId, messageId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deleteInboxMessage(id: String): Flowable<Void> {
        return apiService.deleteInboxMessage(id).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?): Flowable<List<Member>> {
        return apiService.getGroupMembers(groupId, includeAllPublicFields).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?, lastId: String): Flowable<List<Member>> {
        return apiService.getGroupMembers(groupId, includeAllPublicFields, lastId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun likeMessage(groupId: String, mid: String): Flowable<ChatMessage> {
        return apiService.likeMessage(groupId, mid).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun flagMessage(groupId: String, mid: String, data: MutableMap<String, String>): Flowable<Void> {
        return apiService.flagMessage(groupId, mid, data).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun flagInboxMessage(mid: String, data: MutableMap<String, String>): Flowable<Void> {
        return apiService.flagInboxMessage(mid, data).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun seenMessages(groupId: String): Flowable<Void> {
        return apiService.seenMessages(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun inviteToGroup(groupId: String, inviteData: Map<String, Any>): Flowable<Void> {
        return apiService.inviteToGroup(groupId, inviteData).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun rejectGroupInvite(groupId: String): Flowable<Void> {
        return apiService.rejectGroupInvite(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun acceptQuest(groupId: String): Flowable<Void> {
        return apiService.acceptQuest(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun rejectQuest(groupId: String): Flowable<Void> {
        return apiService.rejectQuest(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun cancelQuest(groupId: String): Flowable<Void> {
        return apiService.cancelQuest(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun forceStartQuest(groupId: String, group: Group): Flowable<Quest> {
        return apiService.forceStartQuest(groupId, group).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun inviteToQuest(groupId: String, questKey: String): Flowable<Quest> {
        return apiService.inviteToQuest(groupId, questKey).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun abortQuest(groupId: String): Flowable<Quest> {
        return apiService.abortQuest(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun leaveQuest(groupId: String): Flowable<Void> {
        return apiService.leaveQuest(groupId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun validatePurchase(request: PurchaseValidationRequest): Flowable<PurchaseValidationResult> {
        return apiService.validatePurchase(request).map { habitResponse ->
            if (habitResponse.notifications != null) {
                notificationsManager.setNotifications(habitResponse.notifications)
            }
            habitResponse.getData()
        }
    }

    override fun changeCustomDayStart(updateObject: Map<String, Any>): Flowable<User> {
        return apiService.changeCustomDayStart(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getMember(memberId: String): Flowable<Member> {
        return apiService.getMember(memberId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getMemberWithUsername(username: String): Flowable<Member> {
        return apiService.getMemberWithUsername(username).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getMemberAchievements(memberId: String): Flowable<List<Achievement>> {
        return apiService.getMemberAchievements(memberId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun findUsernames(username: String, context: String?, id: String?): Flowable<List<FindUsernameResult>> {
        return apiService.findUsernames(username, context, id).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun postPrivateMessage(messageDetails: Map<String, String>): Flowable<PostChatMessageResult> {
        return apiService.postPrivateMessage(messageDetails).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun retrieveShopIventory(identifier: String): Flowable<Shop> {
        return apiService.retrieveShopInventory(identifier).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun addPushDevice(pushDeviceData: Map<String, String>): Flowable<List<Void>> {
        return apiService.addPushDevice(pushDeviceData).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deletePushDevice(regId: String): Flowable<List<Void>> {
        return apiService.deletePushDevice(regId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getUserChallenges(page: Int, memberOnly: Boolean): Flowable<List<Challenge>> {
        return if (memberOnly) {
            apiService.getUserChallenges(page, memberOnly).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
        } else {
            apiService.getUserChallenges(page).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
        }
    }

    override fun getChallengeTasks(challengeId: String): Flowable<TaskList> {
        return apiService.getChallengeTasks(challengeId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun getChallenge(challengeId: String): Flowable<Challenge> {
        return apiService.getChallenge(challengeId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun joinChallenge(challengeId: String): Flowable<Challenge> {
        return apiService.joinChallenge(challengeId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun leaveChallenge(challengeId: String, body: LeaveChallengeBody): Flowable<Void> {
        return apiService.leaveChallenge(challengeId, body).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }


    override fun createChallenge(challenge: Challenge): Flowable<Challenge> {
        return apiService.createChallenge(challenge).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun createChallengeTasks(challengeId: String, tasks: List<Task>): Flowable<List<Task>> {
        return apiService.createChallengeTasks(challengeId, tasks).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun createChallengeTask(challengeId: String, task: Task): Flowable<Task> {
        return apiService.createChallengeTask(challengeId, task).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateChallenge(challenge: Challenge): Flowable<Challenge> {
        return apiService.updateChallenge(challenge.id
                ?: "", challenge).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deleteChallenge(challengeId: String): Flowable<Void> {
        return apiService.deleteChallenge(challengeId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun debugAddTenGems(): Flowable<Void> {
        return apiService.debugAddTenGems().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun readNotification(notificationId: String): Flowable<List<*>> {
        return apiService.readNotification(notificationId).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<*>> {
        return apiService.readNotifications(notificationIds).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<*>> {
        return apiService.seeNotifications(notificationIds).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override val content: Flowable<ContentResult>
        get() = apiService.getContent(languageCode).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())

    override fun openMysteryItem(): Flowable<Equipment> {
        return apiService.openMysteryItem().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun runCron(): Flowable<Void> {
        return apiService.runCron().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun resetAccount(): Flowable<Void> {
        return apiService.resetAccount().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun deleteAccount(password: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["password"] = password
        return apiService.deleteAccount(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun togglePinnedItem(pinType: String, path: String): Flowable<Void> {
        return apiService.togglePinnedItem(pinType, path).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun sendPasswordResetEmail(email: String): Flowable<Void> {
        val data = HashMap<String, String>()
        data["email"] = email
        return apiService.sendPasswordResetEmail(data).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateLoginName(newLoginName: String, password: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["username"] = newLoginName
        updateObject["password"] = password
        return apiService.updateLoginName(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateUsername(newLoginName: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["username"] = newLoginName
        return apiService.updateLoginName(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun verifyUsername(username: String): Flowable<VerifyUsernameResponse> {
        val updateObject = HashMap<String, String>()
        updateObject["username"] = username
        return this.apiService.verifyUsername(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updateEmail(newEmail: String, password: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["newEmail"] = newEmail
        updateObject["password"] = password
        return apiService.updateEmail(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["password"] = oldPassword
        updateObject["newPassword"] = newPassword
        updateObject["confirmPassword"] = newPasswordConfirmation
        return apiService.updatePassword(updateObject).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun allocatePoint(stat: String): Flowable<Stats> {
        return apiService.allocatePoint(stat).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun transferGems(giftedID: String, amount: Int): Flowable<Void> {
        return apiService.transferGems(mapOf(Pair("toUserId", giftedID), Pair("gemAmount", amount))).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun bulkAllocatePoints(strength: Int, intelligence: Int, constitution: Int, perception: Int): Flowable<Stats> {
        val body = HashMap<String, Map<String, Int>>()
        val stats = HashMap<String, Int>()
        stats["str"] = strength
        stats["int"] = intelligence
        stats["con"] = constitution
        stats["per"] = perception
        body["stats"] = stats
        return apiService.bulkAllocatePoints(body).compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun retrieveMarketGear(): Flowable<Shop> {
        return apiService.retrieveMarketGear().compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())
    }

    override fun syncOfflineChanges() {
        if (offlineClient.hasPendingRequest()) {
            offlineClient.trySubmitPendingRequests()
        }
    }

    override val worldState: Flowable<WorldState>
        get() = apiService.worldState.compose(configureApiCallObserver()).compose(configureApiOnlineErrorHandler())

    companion object {
        fun createGsonFactory(): GsonConverterFactory {
            return GSonFactoryCreator.create()
        }
    }
}
