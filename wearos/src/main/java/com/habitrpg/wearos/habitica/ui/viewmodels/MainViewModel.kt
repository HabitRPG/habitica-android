package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(userRepository: UserRepository) : BaseViewModel(userRepository) {
    val user = MutableLiveData<User>()

    init {
        viewModelScope.launch {
            user.value = userRepository.retrieveUser()
        }
    }
}