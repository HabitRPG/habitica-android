package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.invitations.PartyInvite
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Named

class MainUserViewModel(val userRepository: UserRepository) {

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var injectedUserID: String

    val formattedUsername: CharSequence?
        get() = user.value?.formattedUsername
    val partyInvitations: List<PartyInvite>
        get() = user.value?.invitations?.parties ?: emptyList()
    val userID: String
        get() = user.value?.id ?: injectedUserID
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

    val user: LiveData<User?>

    init {
        HabiticaBaseApplication.userComponent?.inject(this)
        user = userRepository.getUser().asLiveData()
    }

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
