package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.invitations.PartyInvite
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainUserViewModel(val userRepository: UserRepository) {

    val formattedUsername: CharSequence?
        get() = user.value?.formattedUsername
    val partyInvitations: List<PartyInvite>
        get() = user.value?.invitations?.parties ?: emptyList()
    val userID: String?
        get() = user.value?.id
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

    private val _user: MutableLiveData<User?> by lazy {
        loadUserFromLocal()
        MutableLiveData<User?>()
    }
    val user: LiveData<User?> by lazy {
        _user
    }

    init {
        HabiticaBaseApplication.userComponent?.inject(this)
    }

    fun onCleared() {
        userRepository.close()
        disposable.clear()
    }

    internal val disposable = CompositeDisposable()

    private fun loadUserFromLocal() {
        disposable.add(
            userRepository.getUser().observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _user.value = it
                           }, RxErrorHandler.handleEmptyError())
        )
    }

    fun updateUser(path: String, value: Any) {
        disposable.add(
            userRepository.updateUser(path, value)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }
}
