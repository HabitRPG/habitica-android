package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainUserViewModel @Inject constructor(private val authenticationHandler: AuthenticationHandler, val userRepository: UserRepository, val socialRepository: SocialRepository) {

    val formattedUsername: CharSequence?
        get() = user.value?.formattedUsername
    val userID: String
        get() = user.value?.id ?: authenticationHandler.currentUserID ?: ""
    val username: CharSequence
        get() = user.value?.username ?: ""
    val displayName: CharSequence
        get() = user.value?.profile?.name ?: ""
    val partyID: String?
        get() = user.value?.party?.id
    val isUserFainted: Boolean
        get() = (user.value?.stats?.hp ?: 1.0) == 0.0
    val isUserInParty: Boolean
        get() = user.value?.hasParty == true
    val mirrorGroupTasks: List<String>
        get() = user.value?.preferences?.tasks?.mirrorGroupTasks ?: emptyList()

    val user: LiveData<User?> = userRepository.getUser().asLiveData()
    var currentTeamPlan = MutableSharedFlow<TeamPlan?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    var currentTeamPlanGroup = currentTeamPlan
        .map { it?.id }
        .distinctUntilChanged { old, new -> old == new }
        .filterNotNull()
        .flatMapLatest { socialRepository.getGroup(it) }

    @OptIn(ExperimentalCoroutinesApi::class)
    var currentTeamPlanMembers: LiveData<List<Member>> = currentTeamPlan
        .map { it?.id }
        .distinctUntilChanged { old, new -> old == new }
        .filterNotNull()
        .flatMapLatest { socialRepository.getGroupMembers(it) }
        .distinctUntilChanged { old, new -> old.size == new.size && !old.mapIndexed { index, member -> member.id == new[index].id }.contains(false) }
        .onEach {
            if (it.isEmpty()) {
                currentTeamPlan.lastOrNull()?.let { plan ->
                    userRepository.retrieveTeamPlan(plan.id)
                }
            }
        }
        .asLiveData()

    fun updateUser(path: String, value: Any) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            userRepository.updateUser(path, value)
        }
    }

    fun updateUser(data: Map<String, Any>) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            userRepository.updateUser(data)
        }
    }
}
