package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.habitrpg.wearos.habitica.data.repositories.UserRepository

open class BaseViewModel(val userRepository: UserRepository): ViewModel()