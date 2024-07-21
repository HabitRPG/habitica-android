package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.apiclient.NotificationsManager
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.toFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

enum class GroupViewType(internal val order: String) {
    PARTY("party"),
    GUILD("guild"),
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
open class GroupViewModel @Inject constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val challengeRepository: ChallengeRepository,
    val socialRepository: SocialRepository,
    val notificationsManager: NotificationsManager,
    ) : BaseViewModel(userRepository, userViewModel) {

        private val _groupIDState = MutableStateFlow<String?>(null)
        val groupIDState: Flow<String?> = _groupIDState.asStateFlow()

        var groupViewType: GroupViewType? = null

        private val groupFlow =
            groupIDState
                .filterNotNull()
                .flatMapLatest { socialRepository.getGroup(it) }
        private val group = groupFlow.asLiveData()

        private val leaderFlow =
            groupFlow.map { it?.leaderID }
                .filterNotNull()
                .flatMapLatest { socialRepository.retrieveMember(it).toFlow() }
        private val leader = leaderFlow.asLiveData()

        private val isMemberFlow =
            groupIDState
                .filterNotNull()
                .flatMapLatest { socialRepository.getGroupMembership(it) }
                .map { it != null }
        private val isMemberData = isMemberFlow.asLiveData()

        private val chatMessagesLiveData: MutableLiveData<List<ChatMessage>> by lazy {
            MutableLiveData<List<ChatMessage>>(listOf())
        }
        val chatmessages: LiveData<List<ChatMessage>> by lazy {
            chatMessagesLiveData
        }

        var gotNewMessages: Boolean = false

        override fun onCleared() {
            socialRepository.close()
            super.onCleared()
        }

        fun setGroupID(groupID: String) {
            if (groupID == _groupIDState.value) return
            _groupIDState.value = groupID

            viewModelScope.launchCatching {
                val notifications =
                    notificationsManager.getNotifications().firstOrNull()?.filter { notification ->
                        val data = notification.data as? NewChatMessageData
                        data?.group?.id == groupID
                    } ?: return@launchCatching
                notifications.forEach { userRepository.readNotification(it.id) }
            }
        }

        val groupID: String?
            get() = _groupIDState.value
        val isMember: Boolean
            get() = isMemberData.value ?: false
        val leaderID: String?
            get() = group.value?.leaderID
        val isLeader: Boolean
            get() = user.value?.id == leaderID
        val isPublicGuild: Boolean
            get() = group.value?.privacy == "public"

        val pendingInvites = mutableStateListOf<Member>()
        val pendingInviteStates = mutableStateMapOf<String, LoadingButtonState>()

        fun getGroupData(): LiveData<Group?> = group

        fun getLeaderData(): LiveData<Member?> = leader

        fun getIsMemberData(): LiveData<Boolean> = isMemberData

        fun retrieveGroup(function: (() -> Unit)?) {
            if (groupID?.isNotEmpty() == true) {
                viewModelScope.launch(
                    ExceptionHandler.coroutine {
                        if (it is HttpException && it.code() == 404) {
                            MainNavigationController.navigateBack()
                        }
                    },
                ) {
                    val group = socialRepository.retrieveGroup(groupID ?: "")
                    if (groupViewType == GroupViewType.PARTY) {
                        socialRepository.retrievePartyMembers(group?.id ?: "", true)
                        if (isLeader) {
                            val invites =
                                socialRepository.retrievegroupInvites(group?.id ?: "", true)
                                    ?: emptyList()
                            pendingInvites.clear()
                            pendingInvites.addAll(invites)
                        }
                    }
                    function?.invoke()
                }
            }
        }

        fun inviteToGroup(inviteData: HashMap<String, Any>) {
            viewModelScope.launchCatching {
                socialRepository.inviteToGroup(group.value?.id ?: "", inviteData)
            }
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
                        bundle?.getBoolean("leaderCreateChallenge"),
                    )
                } else {
                    socialRepository.updateGroup(
                        group.value,
                        bundle?.getString("name"),
                        bundle?.getString("description"),
                        bundle?.getString("leader"),
                        bundle?.getBoolean("leaderOnlyChallenges"),
                    )
                }
            }
        }

        fun leaveGroup(
            groupChallenges: List<Challenge>,
            keepChallenges: Boolean = true,
            function: (() -> Unit)? = null,
        ) {
            if (!keepChallenges) {
                viewModelScope.launchCatching {
                    for (challenge in groupChallenges) {
                        challengeRepository.leaveChallenge(challenge, "remove-all")
                    }
                }
            }
            viewModelScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.leaveGroup(groupID ?: "", keepChallenges)
                userRepository.retrieveUser(withTasks = false, forced = true)
                function?.invoke()
            }
        }

        fun joinGroup(
            id: String? = null,
            function: (() -> Unit)? = null,
        ) {
            viewModelScope.launchCatching {
                socialRepository.joinGroup(id ?: groupID)
                function?.invoke()
            }
        }

        fun rejectGroupInvite(id: String? = null) {
            groupID?.let {
                viewModelScope.launchCatching {
                    socialRepository.rejectGroupInvite(id ?: it)
                }
            }
        }

        fun markMessagesSeen() {
            groupID?.let {
                if (it.isNotEmpty() && gotNewMessages) {
                    viewModelScope.launchCatching {
                        socialRepository.markMessagesSeen(it)
                    }
                }
            }
        }

        fun likeMessage(message: ChatMessage) {
            viewModelScope.launchCatching {
                val newMessage = socialRepository.likeMessage(message)
                val index = chatMessagesLiveData.value?.indexOfFirst { it.id == newMessage?.id }
                if (index == null || index < 0) {
                    retrieveGroupChat { }
                    return@launchCatching
                }
                val list = mutableListOf<ChatMessage>()
                chatMessagesLiveData.value?.let { list.addAll(it) }
                if (newMessage != null) {
                    list[index] = newMessage
                }
                chatMessagesLiveData.postValue(list)
            }
        }

        fun deleteMessage(chatMessage: ChatMessage) {
            val oldIndex = chatMessagesLiveData.value?.indexOf(chatMessage) ?: return
            val list = chatMessagesLiveData.value?.toMutableList()
            list?.remove(chatMessage)
            chatMessagesLiveData.postValue(list)
            viewModelScope.launch(
                ExceptionHandler.coroutine {
                    list?.add(oldIndex, chatMessage)
                    chatMessagesLiveData.postValue(list)
                    ExceptionHandler.reportError(it)
                },
            ) {
                socialRepository.deleteMessage(chatMessage)
            }
        }

        fun postGroupChat(
            chatText: String,
            onComplete: () -> Unit,
            onError: () -> Unit,
        ) {
            groupID?.let { groupID ->
                viewModelScope.launch(
                    ExceptionHandler.coroutine {
                        ExceptionHandler.reportError(it)
                        onError()
                    },
                ) {
                    val response = socialRepository.postGroupChat(groupID, chatText)
                    val list = chatMessagesLiveData.value?.toMutableList()
                    if (response != null) {
                        list?.add(0, response.message)
                    }
                    chatMessagesLiveData.postValue(list)
                    onComplete()
                }
            }
        }

        fun retrieveGroupChat(onComplete: () -> Unit) {
            var groupID = groupID
            if (groupViewType == GroupViewType.PARTY) {
                groupID = "party"
            }
            if (groupID.isNullOrEmpty()) {
                onComplete()
                return
            }
            viewModelScope.launch(ExceptionHandler.coroutine()) {
                val messages = socialRepository.retrieveGroupChat(groupID)
                chatMessagesLiveData.postValue(messages)
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
                    bundle?.getBoolean("leaderOnlyChallenges"),
                )
            }
        }

        fun rescindInvite(invitedMember: Member) {
            pendingInviteStates[invitedMember.id] = LoadingButtonState.LOADING
            viewModelScope.launchCatching({
                pendingInviteStates[invitedMember.id] = LoadingButtonState.FAILED
            }) {
                socialRepository.removeMemberFromGroup(groupID ?: "", invitedMember.id)
                pendingInviteStates[invitedMember.id] = LoadingButtonState.SUCCESS
                delay(1.toDuration(DurationUnit.SECONDS))
                pendingInvites.remove(invitedMember)
            }
        }
    }
