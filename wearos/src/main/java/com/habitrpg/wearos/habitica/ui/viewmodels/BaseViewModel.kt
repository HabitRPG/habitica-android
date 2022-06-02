package com.habitrpg.wearos.habitica.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel: ViewModel() {
    @Inject
    lateinit var repository: UserRepository
}