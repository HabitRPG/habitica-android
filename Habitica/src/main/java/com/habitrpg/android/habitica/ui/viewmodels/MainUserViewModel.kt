package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.invitations.PartyInvite
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class MainUserViewModel(private val providedUserID: String, val userRepository: UserRepository, val socialRepository: SocialRepository) {


    val formattedUsername: CharSequence?
        get() = user.value?.formattedUsername
    val partyInvitations: List<PartyInvite>
        get() = user.value?.invitations?.parties ?: emptyList()
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
    var currentTeamPlan: MutableStateFlow<TeamPlan?> = MutableStateFlow(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    var currentTeamPlanGroup = currentTeamPlan
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.id == new.id }
        .flatMapLatest { socialRepository.getGroup(it.id) }

    fun onCleared() {
        userRepository.close()
        disposable.clear()
    }

    internal val disposable = CompositeDisposable()

    fun updateUser(path: String, value: Any) {
        disposable.add(
            userRepository.updateUser(path, value)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }

    fun updateUser(data: Map<String, Any>) {
        disposable.add(
            userRepository.updateUser(data)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }
}
