package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

    protected val groupIDState = MutableStateFlow<String?>(null)
    val groupIDFlow: Flow<String?> = groupIDState

    var groupViewType: GroupViewType? = null

    private val groupFlow = groupIDFlow
        .filterNotNull()
        .flatMapLatest { socialRepository.getGroup(it) }
    private val group = groupFlow.asLiveData()


    private val leaderFlow = groupFlow.map { it?.leaderID }
        .filterNotNull()
        .flatMapLatest { socialRepository.retrieveMember(it).toFlow() }
    private val leader = leaderFlow.asLiveData()


    private val isMemberFlow = groupIDFlow
        .filterNotNull()
        .flatMapLatest { socialRepository.getGroupMembership(it) }
        .map { it != null }
    private val isMemberData = isMemberFlow.asLiveData()

    private val _chatMessages: MutableLiveData<List<ChatMessage>> by lazy {
        MutableLiveData<List<ChatMessage>>(listOf())
    }
    val chatmessages: LiveData<List<ChatMessage>> by lazy {
        _chatMessages
    }

    var gotNewMessages: Boolean = false

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    override fun onCleared() {
        socialRepository.close()
        super.onCleared()
    }

    fun setGroupID(groupID: String) {
        if (groupID == groupIDState.value) return
        groupIDState.value = groupID

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
                    ExceptionHandler.rx()
                )
        )
    }

    val groupID: String?
        get() = groupIDState.value
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
    fun getIsMemberData(): LiveData<Boolean> = isMemberData

    fun retrieveGroup(function: (() -> Unit)?) {
        if (groupID?.isNotEmpty() == true) {
            viewModelScope.launch(ExceptionHandler.coroutine {
                if (it is HttpException && it.code() == 404) {
                    MainNavigationController.navigateBack()
                }
            }) {
                val group = socialRepository.retrieveGroup(groupID ?: "")
                if (groupViewType == GroupViewType.PARTY) {
                    socialRepository.retrievePartyMembers(group?.id ?: "", true)
                }
                function?.invoke()
            }
        }
    }

    fun inviteToGroup(inviteData: HashMap<String, Any>) {
        disposable.add(
            socialRepository.inviteToGroup(group.value?.id ?: "", inviteData)
                .subscribe({ }, ExceptionHandler.rx())
        )
    }

    fun updateOrCreateGroup(bundle: Bundle?) {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
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
                    socialRepository.updateGroup(
                        group.value, bundle?.getString("name"),
                        bundle?.getString("description"),
                        bundle?.getString("leader"),
                        bundle?.getBoolean("leaderCreateChallenge")
                    )
            }
        }
    }

    fun leaveGroup(
        groupChallenges: List<Challenge>,
        keepChallenges: Boolean = true,
        function: (() -> Unit)? = null
    ) {
        if (!keepChallenges) {
            for (challenge in groupChallenges) {
                challengeRepository.leaveChallenge(challenge, "remove-all").subscribe({}, ExceptionHandler.rx())
            }
        }
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.leaveGroup(groupID ?: "", keepChallenges)
            userRepository.retrieveUser(withTasks = false, forced = true)
        }
    }

    fun joinGroup(id: String? = null, function: (() -> Unit)? = null) {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.joinGroup(id ?: groupID)
            function?.invoke()
        }
    }

    fun rejectGroupInvite(id: String? = null) {
        groupID?.let {
            disposable.add(socialRepository.rejectGroupInvite(id ?: it).subscribe({ }, ExceptionHandler.rx()))
        }
    }

    fun markMessagesSeen() {
        groupID?.let {
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
                }, ExceptionHandler.rx()
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
                ExceptionHandler.reportError(it)
            })
        )
    }

    fun postGroupChat(chatText: String, onComplete: () -> Unit, onError: () -> Unit) {
        groupID?.let { groupID ->
            socialRepository.postGroupChat(groupID, chatText).subscribe(
                {
                    val list = _chatMessages.value?.toMutableList()
                    list?.add(0, it.message)
                    _chatMessages.postValue(list)
                    onComplete()
                },
                { error ->
                    ExceptionHandler.reportError(error)
                    onError()
                }
            )
        }
    }

    fun retrieveGroupChat(onComplete: () -> Unit) {
        val groupID = groupID
        if (groupID.isNullOrEmpty()) {
            onComplete()
            return
        }
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            val messages = socialRepository.retrieveGroupChat(groupID)
            _chatMessages.postValue(messages)
            onComplete()
        }
    }

    fun updateGroup(bundle: Bundle?) {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.updateGroup(
                group.value,
                bundle?.getString("name"),
                bundle?.getString("description"),
                bundle?.getString("leader"),
                bundle?.getBoolean("leaderOnlyChallenges")
            )
        }
    }
}
