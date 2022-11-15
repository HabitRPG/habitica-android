package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.amplitude.api.Amplitude
import com.google.gson.JsonSyntaxException
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.ApiService
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.BulkTaskScoringData
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.api.Server
import com.habitrpg.common.habitica.models.HabitResponse
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import com.habitrpg.common.habitica.models.PurchaseValidationResult
import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.common.habitica.models.auth.UserAuthSocialTokens
import com.habitrpg.shared.habitica.models.responses.ErrorResponse
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import com.habitrpg.shared.habitica.models.responses.Status
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.VerifyUsernameResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableTransformer
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

class ApiClientImpl(
    private val converter: Converter.Factory,
    override val hostConfig: HostConfig,
    private val analyticsManager: AnalyticsManager,
    private val notificationsManager: NotificationsManager,
    private val context: Context
) : Consumer<Throwable>, ApiClient {

    private lateinit var retrofitAdapter: Retrofit

    // I think we don't need the ApiClientImpl anymore we could just use ApiService
    private lateinit var apiService: ApiService

    private val apiCallTransformer = FlowableTransformer<HabitResponse<Any>, Any> { observable ->
        observable
            .filter { it.data != null }
            .map { habitResponse ->
                processResponse(habitResponse)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(this)
    }

    private fun <T> processResponse(habitResponse: HabitResponse<T>): T? {
        habitResponse.notifications?.let {
            notificationsManager.setNotifications(it)
        }
        return habitResponse.data
    }

    suspend fun <T> handleSuspendCall(apiCall: suspend () -> HabitResponse<T>): T? {
        try {
            return processResponse(apiCall())
        } catch (throwable: Throwable) {
            accept(throwable)
        }
        return null
    }

    private var languageCode: String? = null
    private var lastAPICallURL: String? = null
    private var hadError = false

    init {
        HabiticaBaseApplication.userComponent?.inject(this)
        analyticsManager.setUserIdentifier(this.hostConfig.userID)
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

        val cacheSize: Long = 10 * 1024 * 1024 // 10 MB

        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        val client = OkHttpClient.Builder()
            .cache(cache)
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
                val request = builder.method(original.method, original.body)
                    .build()
                lastAPICallURL = original.url.toString()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .readTimeout(2400, TimeUnit.SECONDS)
            .build()

        val server = Server(this.hostConfig.address)

        retrofitAdapter = Retrofit.Builder()
            .client(client)
            .baseUrl(server.toString())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(converter)
            .build()

        this.apiService = retrofitAdapter.create(ApiService::class.java)
    }

    override fun updateServerUrl(newAddress: String?) {
        if (newAddress != null) {
            hostConfig.address = newAddress
            buildRetrofit()
        }
    }

    override fun registerUser(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flowable<UserAuthResponse> {
        val auth = UserAuth()
        auth.username = username
        auth.password = password
        auth.confirmPassword = confirmPassword
        auth.email = email
        return this.apiService.registerUser(auth).compose(configureApiCallObserver())
    }

    override fun connectUser(username: String, password: String): Flowable<UserAuthResponse> {
        val auth = UserAuth()
        auth.username = username
        auth.password = password
        return this.apiService.connectLocal(auth).compose(configureApiCallObserver())
    }

    override fun connectSocial(network: String, userId: String, accessToken: String): Flowable<UserAuthResponse> {
        val auth = UserAuthSocial()
        auth.network = network
        val authResponse = UserAuthSocialTokens()
        authResponse.client_id = userId
        authResponse.access_token = accessToken
        auth.authResponse = authResponse

        return this.apiService.connectSocial(auth).compose(configureApiCallObserver())
    }

    override fun disconnectSocial(network: String): Flowable<Void> {
        return this.apiService.disconnectSocial(network).compose(configureApiCallObserver())
    }

    override fun loginApple(authToken: String): Flowable<UserAuthResponse> {
        return apiService.loginApple(mapOf(Pair("code", authToken))).compose(configureApiCallObserver())
    }

    override fun accept(throwable: Throwable) {
        val throwableClass = throwable.javaClass
        if (SocketTimeoutException::class.java.isAssignableFrom(throwableClass)) {
            return
        }
        @Suppress("DEPRECATION")
        if (SocketException::class.java.isAssignableFrom(throwableClass) || SSLException::class.java.isAssignableFrom(throwableClass)) {
            this.showConnectionProblemDialog(R.string.internal_error_api)
        } else if (throwableClass == SocketTimeoutException::class.java || UnknownHostException::class.java == throwableClass || IOException::class.java == throwableClass) {
            this.showConnectionProblemDialog(R.string.network_error_no_network_body)
        } else if (retrofit2.adapter.rxjava3.HttpException::class.java.isAssignableFrom(throwable.javaClass) || HttpException::class.java.isAssignableFrom(throwable.javaClass)) {
            val error = throwable as HttpException
            val res = getErrorResponse(error)
            val status = error.code()

            if (res.message != null && res.message == "RECEIPT_ALREADY_USED") {
                return
            }
            if (error.response()?.raw()?.request?.url?.toString()?.endsWith("/user/push-devices") == true) {
                // workaround for an error that sometimes displays that the user already has this push device
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
            analyticsManager.logError("Json Error: " + lastAPICallURL + ",  " + throwable.message)
        } else {
            analyticsManager.logException(throwable)
        }
    }

    override fun getErrorResponse(throwable: HttpException): ErrorResponse {
        val errorResponse = throwable.response()?.errorBody() ?: return ErrorResponse()
        val errorConverter = converter
            .responseBodyConverter(ErrorResponse::class.java, arrayOfNulls(0), retrofitAdapter)
        return try {
            errorConverter?.convert(errorResponse) as ErrorResponse
        } catch (e: IOException) {
            analyticsManager.logError("Json Error: " + lastAPICallURL + ",  " + e.message)
            ErrorResponse()
        }
    }

    override suspend fun retrieveUser(withTasks: Boolean): User? {
        val user = handleSuspendCall { apiService.getUser() }
        val tasks = getTasks()
        user?.tasks = tasks
        return user
    }

    override suspend fun retrieveInboxMessages(uuid: String, page: Int): List<ChatMessage>? {
        return handleSuspendCall { apiService.getInboxMessages(uuid, page) }
    }

    override fun retrieveInboxConversations(): Flowable<List<InboxConversation>> {
        return apiService.getInboxConversations().compose(configureApiCallObserver())
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

    private fun showConnectionProblemDialog(
        resourceTitleString: String?,
        resourceMessageString: String
    ) {
        hadError = true
        val application = (context as? HabiticaBaseApplication)
            ?: (context.applicationContext as? HabiticaBaseApplication)
        application?.currentActivity?.get()
            ?.showConnectionProblem(resourceTitleString, resourceMessageString)
    }

    private fun hideConnectionProblemDialog() {
        hadError = false
        val application = (context as? HabiticaBaseApplication)
            ?: (context.applicationContext as? HabiticaBaseApplication)
        application?.currentActivity?.get()
            ?.hideConnectionProblem()
    }

    /*
     This function is used with Observer.compose to reuse transformers across the application.
     See here for more info: http://blog.danlew.net/2015/03/02/dont-break-the-chain/
     */

    override fun <T : Any> configureApiCallObserver(): FlowableTransformer<HabitResponse<T>, T> {
        @Suppress("UNCHECKED_CAST")
        return apiCallTransformer as FlowableTransformer<HabitResponse<T>, T>
    }

    override fun updateAuthenticationCredentials(userID: String?, apiToken: String?) {
        this.hostConfig.userID = userID ?: ""
        this.hostConfig.apiKey = apiToken ?: ""
        analyticsManager.setUserIdentifier(this.hostConfig.userID)
        Amplitude.getInstance().userId = this.hostConfig.userID
    }

    override fun setLanguageCode(languageCode: String) {
        this.languageCode = languageCode
    }

    override suspend fun getStatus(): Status? = handleSuspendCall { apiService.getStatus() }

    override suspend fun getContent(language: String?): ContentResult? {
        return handleSuspendCall {  apiService.getContent(language) }
    }

    override fun updateUser(updateDictionary: Map<String, Any>): Flowable<User> {
        return apiService.updateUser(updateDictionary).compose(configureApiCallObserver())
    }

    override fun registrationLanguage(registrationLanguage: String): Flowable<User> {
        return apiService.registrationLanguage(registrationLanguage).compose(configureApiCallObserver())
    }

    override fun retrieveInAppRewards(): Flowable<List<ShopItem>> {
        return apiService.retrieveInAppRewards().compose(configureApiCallObserver())
    }

    override fun retrieveOldGear(): Flowable<List<ShopItem>> {
        return apiService.retrieveOldGearRewards().compose(configureApiCallObserver())
    }

    override fun equipItem(type: String, itemKey: String): Flowable<Items> {
        return apiService.equipItem(type, itemKey).compose(configureApiCallObserver())
    }

    override fun buyItem(itemKey: String, purchaseQuantity: Int): Flowable<BuyResponse> {
        return apiService.buyItem(itemKey, mapOf(Pair("quantity", purchaseQuantity))).compose(configureApiCallObserver())
    }

    override fun unlinkAllTasks(challengeID: String?, keepOption: String): Flowable<Void> {
        return apiService.unlinkAllTasks(challengeID, keepOption).compose(configureApiCallObserver())
    }

    override fun blockMember(userID: String): Flowable<List<String>> {
        return apiService.blockMember(userID).compose(configureApiCallObserver())
    }

    override fun purchaseItem(type: String, itemKey: String, purchaseQuantity: Int): Flowable<Void> {
        return apiService.purchaseItem(type, itemKey, mapOf(Pair("quantity", purchaseQuantity))).compose(configureApiCallObserver())
    }

    override fun validateSubscription(request: PurchaseValidationRequest): Flowable<Any> {
        return apiService.validateSubscription(request).compose(configureApiCallObserver())
    }

    override fun validateNoRenewSubscription(request: PurchaseValidationRequest): Flowable<Any> {
        return apiService.validateNoRenewSubscription(request).compose(configureApiCallObserver())
    }

    override suspend fun cancelSubscription(): Void? {
        return processResponse(apiService.cancelSubscription())
    }

    override fun purchaseHourglassItem(type: String, itemKey: String): Flowable<Void> {
        return apiService.purchaseHourglassItem(type, itemKey).compose(configureApiCallObserver())
    }

    override fun purchaseMysterySet(itemKey: String): Flowable<Void> {
        return apiService.purchaseMysterySet(itemKey).compose(configureApiCallObserver())
    }

    override fun purchaseQuest(key: String): Flowable<Void> {
        return apiService.purchaseQuest(key).compose(configureApiCallObserver())
    }

    override fun purchaseSpecialSpell(key: String): Flowable<Void> {
        return apiService.purchaseSpecialSpell(key).compose(configureApiCallObserver())
    }

    override fun sellItem(itemType: String, itemKey: String): Flowable<User> {
        return apiService.sellItem(itemType, itemKey).compose(configureApiCallObserver())
    }

    override fun feedPet(petKey: String, foodKey: String): Flowable<FeedResponse> {
        return apiService.feedPet(petKey, foodKey)
            .map {
                it.data?.message = it.message
                it
            }
            .compose(configureApiCallObserver())
    }

    override fun hatchPet(eggKey: String, hatchingPotionKey: String): Flowable<Items> {
        return apiService.hatchPet(eggKey, hatchingPotionKey).compose(configureApiCallObserver())
    }

    override suspend fun getTasks(): TaskList? = handleSuspendCall { apiService.getTasks() }

    override fun getTasks(type: String): Flowable<TaskList> {
        return apiService.getTasks(type).compose(configureApiCallObserver())
    }

    override fun getTasks(type: String, dueDate: String): Flowable<TaskList> {
        return apiService.getTasks(type, dueDate).compose(configureApiCallObserver())
    }

    override fun unlockPath(path: String): Flowable<UnlockResponse> {
        return apiService.unlockPath(path).compose(configureApiCallObserver())
    }

    override fun getTask(id: String): Flowable<Task> {
        return apiService.getTask(id).compose(configureApiCallObserver())
    }

    override suspend fun postTaskDirection(id: String, direction: String): TaskDirectionData? {
        return handleSuspendCall { apiService.postTaskDirection(id, direction) }
    }

    override fun bulkScoreTasks(data: List<Map<String, String>>): Flowable<BulkTaskScoringData> {
        return apiService.bulkScoreTasks(data).compose(configureApiCallObserver())
    }

    override fun postTaskNewPosition(id: String, position: Int): Flowable<List<String>> {
        return apiService.postTaskNewPosition(id, position).compose(configureApiCallObserver())
    }

    override suspend fun scoreChecklistItem(taskId: String, itemId: String): Task? {
        return handleSuspendCall { apiService.scoreChecklistItem(taskId, itemId) }
    }

    override fun createTask(item: Task): Flowable<Task> {
        return apiService.createTask(item).compose(configureApiCallObserver())
    }

    override fun createTasks(tasks: List<Task>): Flowable<List<Task>> {
        return apiService.createTasks(tasks).compose(configureApiCallObserver())
    }

    override fun updateTask(id: String, item: Task): Flowable<Task> {
        return apiService.updateTask(id, item).compose(configureApiCallObserver())
    }

    override fun deleteTask(id: String): Flowable<Void> {
        return apiService.deleteTask(id).compose(configureApiCallObserver())
    }

    override fun createTag(tag: Tag): Flowable<Tag> {
        return apiService.createTag(tag).compose(configureApiCallObserver())
    }

    override fun updateTag(id: String, tag: Tag): Flowable<Tag> {
        return apiService.updateTag(id, tag).compose(configureApiCallObserver())
    }

    override fun deleteTag(id: String): Flowable<Void> {
        return apiService.deleteTag(id).compose(configureApiCallObserver())
    }

    override suspend fun sleep(): Boolean? = handleSuspendCall { apiService.sleep() }

    override suspend fun revive(): User? = handleSuspendCall { apiService.revive() }

    override fun useSkill(skillName: String, targetType: String, targetId: String): Flowable<SkillResponse> {
        return apiService.useSkill(skillName, targetType, targetId).compose(configureApiCallObserver())
    }

    override fun useSkill(skillName: String, targetType: String): Flowable<SkillResponse> {
        return apiService.useSkill(skillName, targetType).compose(configureApiCallObserver())
    }

    override suspend fun changeClass(className: String?): User? {
        return handleSuspendCall {
            if (className != null) {
                apiService.changeClass(className)
            } else {
                apiService.changeClass()
            }
        }
    }

    override suspend fun disableClasses(): User? = handleSuspendCall { apiService.disableClasses() }

    override fun markPrivateMessagesRead(): Flowable<Void> {
        // This is necessary, because the API call returns weird data.
        return apiService.markPrivateMessagesRead()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(this)
    }

    override fun listGroups(type: String): Flowable<List<Group>> {
        return apiService.listGroups(type).compose(configureApiCallObserver())
    }

    override suspend fun getGroup(groupId: String): Group? {
        return processResponse(apiService.getGroup(groupId))
    }

    override suspend fun createGroup(group: Group): Group? {
        return processResponse(apiService.createGroup(group))
    }

    override suspend fun updateGroup(id: String, item: Group): Group? {
        return processResponse(apiService.updateGroup(id, item))
    }

    override suspend fun removeMemberFromGroup(groupID: String, userID: String): Void? {
        return processResponse(apiService.removeMemberFromGroup(groupID, userID))
    }

    override suspend fun listGroupChat(groupId: String): List<ChatMessage>? {
        return processResponse(apiService.listGroupChat(groupId))
    }

    override suspend fun joinGroup(groupId: String): Group? {
        return processResponse(apiService.joinGroup(groupId))
    }

    override suspend fun leaveGroup(groupId: String, keepChallenges: String): Void? {
        return processResponse(apiService.leaveGroup(groupId, keepChallenges))
    }

    override fun postGroupChat(groupId: String, message: Map<String, String>): Flowable<PostChatMessageResult> {
        return apiService.postGroupChat(groupId, message).compose(configureApiCallObserver())
    }

    override fun deleteMessage(groupId: String, messageId: String): Flowable<Void> {
        return apiService.deleteMessage(groupId, messageId).compose(configureApiCallObserver())
    }
    override fun deleteInboxMessage(id: String): Flowable<Void> {
        return apiService.deleteInboxMessage(id).compose(configureApiCallObserver())
    }

    override suspend fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?): List<Member>? {
        return processResponse(apiService.getGroupMembers(groupId, includeAllPublicFields))
    }

    override suspend fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?, lastId: String): List<Member>? {
        return processResponse(apiService.getGroupMembers(groupId, includeAllPublicFields, lastId))
    }

    override fun likeMessage(groupId: String, mid: String): Flowable<ChatMessage> {
        return apiService.likeMessage(groupId, mid).compose(configureApiCallObserver())
    }

    override fun flagMessage(groupId: String, mid: String, data: MutableMap<String, String>): Flowable<Void> {
        return apiService.flagMessage(groupId, mid, data).compose(configureApiCallObserver())
    }

    override fun flagInboxMessage(mid: String, data: MutableMap<String, String>): Flowable<Void> {
        return apiService.flagInboxMessage(mid, data).compose(configureApiCallObserver())
    }

    override fun seenMessages(groupId: String): Flowable<Void> {
        return apiService.seenMessages(groupId).compose(configureApiCallObserver())
    }

    override fun inviteToGroup(groupId: String, inviteData: Map<String, Any>): Flowable<List<Void>> {
        return apiService.inviteToGroup(groupId, inviteData).compose(configureApiCallObserver())
    }

    override fun rejectGroupInvite(groupId: String): Flowable<Void> {
        return apiService.rejectGroupInvite(groupId).compose(configureApiCallObserver())
    }

    override fun acceptQuest(groupId: String): Flowable<Void> {
        return apiService.acceptQuest(groupId).compose(configureApiCallObserver())
    }

    override fun rejectQuest(groupId: String): Flowable<Void> {
        return apiService.rejectQuest(groupId).compose(configureApiCallObserver())
    }

    override fun cancelQuest(groupId: String): Flowable<Void> {
        return apiService.cancelQuest(groupId).compose(configureApiCallObserver())
    }

    override fun forceStartQuest(groupId: String, group: Group): Flowable<Quest> {
        return apiService.forceStartQuest(groupId, group).compose(configureApiCallObserver())
    }

    override fun inviteToQuest(groupId: String, questKey: String): Flowable<Quest> {
        return apiService.inviteToQuest(groupId, questKey).compose(configureApiCallObserver())
    }

    override fun abortQuest(groupId: String): Flowable<Quest> {
        return apiService.abortQuest(groupId).compose(configureApiCallObserver())
    }

    override fun leaveQuest(groupId: String): Flowable<Void> {
        return apiService.leaveQuest(groupId).compose(configureApiCallObserver())
    }

    override fun validatePurchase(request: PurchaseValidationRequest): Flowable<PurchaseValidationResult> {
        return apiService.validatePurchase(request).compose(configureApiCallObserver())
    }

    override fun changeCustomDayStart(updateObject: Map<String, Any>): Flowable<User> {
        return apiService.changeCustomDayStart(updateObject).compose(configureApiCallObserver())
    }

    override suspend fun getMember(memberId: String) = processResponse(apiService.getMember(memberId))
    override suspend fun getMemberWithUsername(username: String) = processResponse(apiService.getMemberWithUsername(username))

    override fun getMemberAchievements(memberId: String): Flowable<List<Achievement>> {
        return apiService.getMemberAchievements(memberId, languageCode).compose(configureApiCallObserver())
    }

    override fun findUsernames(username: String, context: String?, id: String?): Flowable<List<FindUsernameResult>> {
        return apiService.findUsernames(username, context, id).compose(configureApiCallObserver())
    }

    override suspend fun postPrivateMessage(messageDetails: Map<String, String>): PostChatMessageResult? {
        return handleSuspendCall { apiService.postPrivateMessage(messageDetails) }
    }

    override fun retrieveShopIventory(identifier: String): Flowable<Shop> {
        return apiService.retrieveShopInventory(identifier, languageCode).compose(configureApiCallObserver())
    }

    override fun addPushDevice(pushDeviceData: Map<String, String>): Flowable<List<Void>> {
        return apiService.addPushDevice(pushDeviceData).compose(configureApiCallObserver())
    }

    override fun deletePushDevice(regId: String): Flowable<List<Void>> {
        return apiService.deletePushDevice(regId).compose(configureApiCallObserver())
    }

    override fun getUserChallenges(page: Int, memberOnly: Boolean): Flowable<List<Challenge>> {
        return if (memberOnly) {
            apiService.getUserChallenges(page, memberOnly).compose(configureApiCallObserver())
        } else {
            apiService.getUserChallenges(page).compose(configureApiCallObserver())
        }
    }

    override fun getChallengeTasks(challengeId: String): Flowable<TaskList> {
        return apiService.getChallengeTasks(challengeId).compose(configureApiCallObserver())
    }

    override fun getChallenge(challengeId: String): Flowable<Challenge> {
        return apiService.getChallenge(challengeId).compose(configureApiCallObserver())
    }

    override fun joinChallenge(challengeId: String): Flowable<Challenge> {
        return apiService.joinChallenge(challengeId).compose(configureApiCallObserver())
    }

    override fun leaveChallenge(challengeId: String, body: LeaveChallengeBody): Flowable<Void> {
        return apiService.leaveChallenge(challengeId, body).compose(configureApiCallObserver())
    }

    override fun createChallenge(challenge: Challenge): Flowable<Challenge> {
        return apiService.createChallenge(challenge).compose(configureApiCallObserver())
    }

    override fun createChallengeTasks(challengeId: String, tasks: List<Task>): Flowable<List<Task>> {
        return apiService.createChallengeTasks(challengeId, tasks).compose(configureApiCallObserver())
    }

    override fun createChallengeTask(challengeId: String, task: Task): Flowable<Task> {
        return apiService.createChallengeTask(challengeId, task).compose(configureApiCallObserver())
    }

    override fun updateChallenge(challenge: Challenge): Flowable<Challenge> {
        return apiService.updateChallenge(challenge.id ?: "", challenge).compose(configureApiCallObserver())
    }

    override fun deleteChallenge(challengeId: String): Flowable<Void> {
        return apiService.deleteChallenge(challengeId).compose(configureApiCallObserver())
    }

    override fun debugAddTenGems(): Flowable<Void> {
        return apiService.debugAddTenGems().compose(configureApiCallObserver())
    }

    override fun readNotification(notificationId: String): Flowable<List<Any>> {
        return apiService.readNotification(notificationId).compose(configureApiCallObserver())
    }

    override fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>> {
        return apiService.readNotifications(notificationIds).compose(configureApiCallObserver())
    }

    override fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>> {
        return apiService.seeNotifications(notificationIds).compose(configureApiCallObserver())
    }

    override fun openMysteryItem(): Flowable<Equipment> {
        return apiService.openMysteryItem().compose(configureApiCallObserver())
    }

    override fun runCron(): Flowable<Void> {
        return apiService.runCron().compose(configureApiCallObserver())
    }

    override suspend fun reroll(): User? = handleSuspendCall { apiService.reroll() }

    override fun resetAccount(): Flowable<Void> {
        return apiService.resetAccount().compose(configureApiCallObserver())
    }

    override fun deleteAccount(password: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["password"] = password
        return apiService.deleteAccount(updateObject).compose(configureApiCallObserver())
    }

    override fun togglePinnedItem(pinType: String, path: String): Flowable<Void> {
        return apiService.togglePinnedItem(pinType, path).compose(configureApiCallObserver())
    }

    override fun sendPasswordResetEmail(email: String): Flowable<Void> {
        val data = HashMap<String, String>()
        data["email"] = email
        return apiService.sendPasswordResetEmail(data).compose(configureApiCallObserver())
    }

    override fun updateLoginName(newLoginName: String, password: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["username"] = newLoginName
        updateObject["password"] = password
        return apiService.updateLoginName(updateObject).compose(configureApiCallObserver())
    }

    override fun updateUsername(newLoginName: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["username"] = newLoginName
        return apiService.updateLoginName(updateObject).compose(configureApiCallObserver())
    }

    override fun verifyUsername(username: String): Flowable<VerifyUsernameResponse> {
        val updateObject = HashMap<String, String>()
        updateObject["username"] = username
        return this.apiService.verifyUsername(updateObject).compose(configureApiCallObserver())
    }

    override fun updateEmail(newEmail: String, password: String): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["newEmail"] = newEmail
        if (password.isNotBlank()) {
            updateObject["password"] = password
        }
        return apiService.updateEmail(updateObject).compose(configureApiCallObserver())
    }

    override fun updatePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): Flowable<Void> {
        val updateObject = HashMap<String, String>()
        updateObject["password"] = oldPassword
        updateObject["newPassword"] = newPassword
        updateObject["confirmPassword"] = newPasswordConfirmation
        return apiService.updatePassword(updateObject).compose(configureApiCallObserver())
    }

    override fun allocatePoint(stat: String): Flowable<Stats> {
        return apiService.allocatePoint(stat).compose(configureApiCallObserver())
    }

    override fun transferGems(giftedID: String, amount: Int): Flowable<Void> {
        return apiService.transferGems(mapOf(Pair("toUserId", giftedID), Pair("gemAmount", amount))).compose(configureApiCallObserver())
    }

    override fun getTeamPlans(): Flowable<List<TeamPlan>> {
        return apiService.getTeamPlans().compose(configureApiCallObserver())
    }

    override suspend fun getTeamPlanTasks(teamID: String): TaskList? {
        return processResponse(apiService.getTeamPlanTasks(teamID))
    }

    override fun bulkAllocatePoints(
        strength: Int,
        intelligence: Int,
        constitution: Int,
        perception: Int
    ): Flowable<Stats> {
        val body = HashMap<String, Map<String, Int>>()
        val stats = HashMap<String, Int>()
        stats["str"] = strength
        stats["int"] = intelligence
        stats["con"] = constitution
        stats["per"] = perception
        body["stats"] = stats
        return apiService.bulkAllocatePoints(body).compose(configureApiCallObserver())
    }

    override fun retrieveMarketGear(): Flowable<Shop> {
        return apiService.retrieveMarketGear(languageCode).compose(configureApiCallObserver())
    }

    override suspend fun getWorldState(): WorldState? = handleSuspendCall { apiService.worldState() }

    companion object {
        fun createGsonFactory(): GsonConverterFactory {
            return GSonFactoryCreator.create()
        }
    }
}
