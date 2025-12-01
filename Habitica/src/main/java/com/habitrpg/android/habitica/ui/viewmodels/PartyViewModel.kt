package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyViewModel
@Inject
constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    challengeRepository: ChallengeRepository,
    socialRepository: SocialRepository,
    notificationsManager: NotificationsManager
) : GroupViewModel(
    userRepository,
    userViewModel,
    challengeRepository,
    socialRepository,
    notificationsManager
) {
    internal val isQuestActive: Boolean
        get() = getGroupData().value?.quest?.active == true

    internal val isUserOnQuest: Boolean
        get() =
            !(
                getGroupData().value?.quest?.members?.none { it.key == user.value?.id }
                    ?: true
                )

    internal val isUserQuestLeader: Boolean
        get() = user.value?.id == getGroupData().value?.quest?.leader

    @OptIn(ExperimentalCoroutinesApi::class)
    private val membersFlow =
        groupIDFlow
            .filterNotNull()
            .flatMapLatest { socialRepository.getPartyMembers(it) }
    private val members = membersFlow.asLiveData()

    init {
        groupViewType = GroupViewType.PARTY
    }

    fun getMembersData() = members

    fun acceptQuest() {
        groupID?.let { groupID ->
            viewModelScope.launchCatching {
                socialRepository.acceptQuest(user.value, groupID)
                socialRepository.retrieveGroup(groupID)
                userRepository.retrieveUser()
            }
        }
    }

    fun rejectQuest() {
        groupID?.let { groupID ->
            viewModelScope.launchCatching {
                socialRepository.rejectQuest(user.value, groupID)
                socialRepository.retrieveGroup(groupID)
                userRepository.retrieveUser()
            }
        }
    }

    fun showParticipantButtons(): Boolean {
        val user = user.value
        return !(user?.party == null || user.party?.quest == null) && !isQuestActive && user.party?.quest?.rsvpNeeded == true
    }

    fun loadPartyID() {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            userRepository.getUser()
                .map { it?.party?.id }
                .distinctUntilChanged()
                .filterNotNull()
                .collect {
                    setGroupID(it)
                }
        }
    }
}
