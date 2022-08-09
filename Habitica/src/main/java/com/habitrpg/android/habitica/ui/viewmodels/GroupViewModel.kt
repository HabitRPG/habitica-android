package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.filterOptionalDoOnEmpty
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.common.habitica.extensions.Optional
import com.habitrpg.common.habitica.extensions.asOptional
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class GroupViewType(internal val order: String) {
    PARTY("party"),
    GUILD("guild"),
    TAVERN("tavern")
}

open class GroupViewModel(initializeComponent: Boolean) : BaseViewModel(initializeComponent) {
    constructor() : this(true)

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var notificationsManager: NotificationsManager

    var groupViewType: GroupViewType? = null

    private val group: MutableLiveData<Group?> by lazy {
        MutableLiveData<Group?>()
    }

    private val leader: MutableLiveData<Member?> by lazy {
        MutableLiveData<Member?>()
    }

    private val isMemberData: MutableLiveData<Boolean?> by lazy {
        MutableLiveData<Boolean?>()
    }

    private val _chatMessages: MutableLiveData<List<ChatMessage>> by lazy {
        MutableLiveData<List<ChatMessage>>(listOf())
    }
    val chatmessages: LiveData<List<ChatMessage>> by lazy {
        _chatMessages
    }

    protected val groupIDSubject = BehaviorSubject.create<Optional<String>>()
    val groupIDFlowable: Flowable<Optional<String>> = groupIDSubject.toFlowable(BackpressureStrategy.BUFFER)
    var gotNewMessages: Boolean = false

    init {
        loadGroupFromLocal()
        loadLeaderFromLocal()
        loadMembershipFromLocal()
    }

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    override fun onCleared() {
        socialRepository.close()
        super.onCleared()
    }

    fun setGroupID(groupID: String) {
        if (groupID == groupIDSubject.value?.value) return
        groupIDSubject.onNext(groupID.asOptional())

        disposable.add(
            notificationsManager.getNotifications().firstElement().map {
                it.filter { notification ->
                    val data = notification.data as? NewChatMessageData
                    data?.group?.id == groupID
                }
            }
                .filter { it.isNotEmpty() }
                .flatMapPublisher { userRepository.readNotification(it.first().id) }
                .subscribe(
                    {
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    val groupID: String?
        get() = groupIDSubject.value?.value
    val isMember: Boolean
        get() = isMemberData.value ?: false
    val leaderID: String?
        get() = group.value?.leaderID
    val isLeader: Boolean
        get() = user.value?.id == leaderID
    val isPublicGuild: Boolean
        get() = group.value?.privacy == "public"

    fun getGroupData(): LiveData<Group?> = group
    fun getLeaderData(): LiveData<Member?> = leader
    fun getIsMemberData(): LiveData<Boolean?> = isMemberData

    private fun loadGroupFromLocal() {
        disposable.add(
            groupIDFlowable
                .filterOptionalDoOnEmpty { group.value = null }
                .flatMap { socialRepository.getGroupFlowable(it) }
                .map { socialRepository.getUnmanagedCopy(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ group.value = it }, RxErrorHandler.handleEmptyError())
        )
    }

    private fun loadLeaderFromLocal() {
        disposable.add(
            groupIDFlowable
                .filterOptionalDoOnEmpty { leader.value = null }
                .flatMap { socialRepository.getGroupFlowable(it) }
                .distinctUntilChanged { group1, group2 -> group1.id == group2.id }
                .flatMap { socialRepository.getMember(it.leaderID) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ leader.value = it }, RxErrorHandler.handleEmptyError())
        )
    }

    private fun loadMembershipFromLocal() {
        disposable.add(
            groupIDFlowable
                .filterOptionalDoOnEmpty { isMemberData.value = null }
                .flatMap { socialRepository.getGroupMemberships() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        isMemberData.value = it.firstOrNull { membership -> membership.groupID == groupID } != null
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    fun retrieveGroup(function: (() -> Unit)?) {
        if (groupID?.isNotEmpty() == true) {
            disposable.add(
                socialRepository.retrieveGroup(groupID ?: "")
                    .filter { groupViewType == GroupViewType.PARTY }
                    .flatMap { group1 ->
                        socialRepository.retrieveGroupMembers(group1.id, true)
                    }
                    .doOnComplete { function?.invoke() }
                    .subscribe({ }, {
                        if (it is HttpException && it.code() == 404) {
                            MainNavigationController.navigateBack()
                        }
                        RxErrorHandler.reportError(it)
                    })
            )
        }
    }

    fun inviteToGroup(inviteData: HashMap<String, Any>) {
        disposable.add(
            socialRepository.inviteToGroup(group.value?.id ?: "", inviteData)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }

    fun updateOrCreateGroup(bundle: Bundle?) {
        if (group.value == null) {
            socialRepository.createGroup(
                bundle?.getString("name"),
                bundle?.getString("description"),
                bundle?.getString("leader"),
                bundle?.getString("groupType"),
                bundle?.getString("privacy"),
                bundle?.getBoolean("leaderCreateChallenge")
            )
        } else {
            disposable.add(
                socialRepository.updateGroup(
                    group.value, bundle?.getString("name"),
                    bundle?.getString("description"),
                    bundle?.getString("leader"),
                    bundle?.getBoolean("leaderCreateChallenge")
                )
                    .subscribe({ }, RxErrorHandler.handleEmptyError())
            )
        }
    }

    fun leaveGroup(
        groupChallenges: List<Challenge>,
        keepChallenges: Boolean = true,
        function: (() -> Unit)? = null
    ) {
        if (!keepChallenges) {
            for (challenge in groupChallenges) {
                challengeRepository.leaveChallenge(challenge, "remove-all").subscribe({}, RxErrorHandler.handleEmptyError())
            }
        }
        disposable.add(
            socialRepository.leaveGroup(this.group.value?.id ?: "", keepChallenges)
                .flatMap { userRepository.retrieveUser(withTasks = false, forced = true) }
                .subscribe(
                    {
                        function?.invoke()
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    fun joinGroup(id: String? = null, function: (() -> Unit)? = null) {
        disposable.add(
            socialRepository.joinGroup(id ?: groupID).subscribe(
                {
                    function?.invoke()
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    fun rejectGroupInvite(id: String? = null) {
        groupID?.let {
            disposable.add(socialRepository.rejectGroupInvite(id ?: it).subscribe({ }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun markMessagesSeen() {
        groupIDSubject.value?.value?.let {
            if (groupViewType != GroupViewType.TAVERN && it.isNotEmpty() && gotNewMessages) {
                socialRepository.markMessagesSeen(it)
            }
        }
    }

    fun likeMessage(message: ChatMessage) {
        val index = _chatMessages.value?.indexOf(message)
        if (index == null || index < 0) return
        disposable.add(
            socialRepository.likeMessage(message).subscribe(
                {
                    val list = _chatMessages.value?.toMutableList()
                    list?.set(index, it)
                    _chatMessages.postValue(list)
                }, RxErrorHandler.handleEmptyError()
            )
        )
    }

    fun deleteMessage(chatMessage: ChatMessage) {
        val oldIndex = _chatMessages.value?.indexOf(chatMessage) ?: return
        val list = _chatMessages.value?.toMutableList()
        list?.remove(chatMessage)
        _chatMessages.postValue(list)
        disposable.add(
            socialRepository.deleteMessage(chatMessage).subscribe({
            }, {
                list?.add(oldIndex, chatMessage)
                _chatMessages.postValue(list)
                RxErrorHandler.reportError(it)
            })
        )
    }

    fun postGroupChat(chatText: String, onComplete: () -> Unit, onError: () -> Unit) {
        groupIDSubject.value?.value?.let { groupID ->
            socialRepository.postGroupChat(groupID, chatText).subscribe(
                {
                    val list = _chatMessages.value?.toMutableList()
                    list?.add(0, it.message)
                    _chatMessages.postValue(list)
                    onComplete()
                },
                { error ->
                    RxErrorHandler.reportError(error)
                    onError()
                }
            )
        }
    }

    fun retrieveGroupChat(onComplete: () -> Unit) {
        val groupID = groupIDSubject.value?.value
        if (groupID.isNullOrEmpty()) {
            onComplete()
            return
        }
        disposable.add(
            socialRepository.retrieveGroupChat(groupID)
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _chatMessages.postValue(it)
                        onComplete()
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    fun updateGroup(bundle: Bundle?) {
        disposable.add(
            socialRepository.updateGroup(
                group.value,
                bundle?.getString("name"),
                bundle?.getString("description"),
                bundle?.getString("leader"),
                bundle?.getBoolean("leaderOnlyChallenges")
            )
                .subscribe({}, RxErrorHandler.handleEmptyError())
        )
    }
}
