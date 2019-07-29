package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import javax.inject.Inject

abstract class BaseViewModel: ViewModel() {

    @Inject
    lateinit var userRepository: UserRepository

    private val user: MutableLiveData<User?> by lazy {
        loadUserFromLocal()
        MutableLiveData<User?>()
    }

    init {
        HabiticaBaseApplication.userComponent?.let { inject(it) }
    }

    abstract fun inject(component: UserComponent)

    override fun onCleared() {
        userRepository.close()
        disposable.clear()
        super.onCleared()
    }

    internal val disposable = CompositeDisposable()

    fun getUserData(): LiveData<User?> = user

    private fun loadUserFromLocal() {
        disposable.add(userRepository.getUser().observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer { user.value = it }, RxErrorHandler.handleEmptyError()))
    }

    fun updateUser(path: String, value: Any) {
        disposable.add(userRepository.updateUser(getUserData().value, path, value).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }
}
