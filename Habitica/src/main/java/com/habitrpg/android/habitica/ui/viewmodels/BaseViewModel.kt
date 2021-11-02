package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseViewModel(initializeComponent: Boolean = true) : ViewModel() {

    @Inject
    lateinit var userRepository: UserRepository

    private val _user: MutableLiveData<User?> by lazy {
        loadUserFromLocal()
        MutableLiveData<User?>()
    }
    val user: LiveData<User?> by lazy {
        _user
    }

    init {
        if (initializeComponent) {
            HabiticaBaseApplication.userComponent?.let { inject(it) }
        }
    }

    abstract fun inject(component: UserComponent)

    override fun onCleared() {
        userRepository.close()
        disposable.clear()
        super.onCleared()
    }

    internal val disposable = CompositeDisposable()

    private fun loadUserFromLocal() {
        disposable.add(userRepository.getUser().observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _user.value = it }, RxErrorHandler.handleEmptyError()))
    }

    fun updateUser(path: String, value: Any) {
        disposable.add(userRepository.updateUser(path, value)
            .subscribe({ }, RxErrorHandler.handleEmptyError()))
    }
}
