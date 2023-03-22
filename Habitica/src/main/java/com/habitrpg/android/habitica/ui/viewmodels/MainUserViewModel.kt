package com.habitrpg.android.habitica.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainUserViewModel @Inject constructor(private val providedUserID: String, val userRepository: UserRepository, val socialRepository: SocialRepository) {

    val formattedUsername: CharSequence?
        get() = user.value?.formattedUsername
    val userID: String
        get() = user.value?.id ?: providedUserID
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
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    var currentTeamPlanGroup = currentTeamPlan
        .filterNotNull()
        .distinctUntilChanged { old, new ->
            Log.d("asfd", "${old.id} - ${new.id}")
            old.id == new.id }
        .flatMapLatest { socialRepository.getGroup(it.id) }
    @OptIn(ExperimentalCoroutinesApi::class)
    var currentTeamPlanMembers: LiveData<List<Member>> = currentTeamPlan
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.id == new.id }
        .flatMapLatest { socialRepository.getGroupMembers(it.id) }
        .asLiveData()

    fun onCleared() {
        userRepository.close()
    }

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
