package com.habitrpg.android.habitica.rpgClassSelectScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClassSelectionViewModel @Inject constructor(
    private val userRepository: UserRepository
) :
    ViewModel(),
    CSVMMethods {

    var state by mutableStateOf(CSVMState())
        private set

    override fun onItemClk(rpgClass: RpgClass) {
        state = state.copy(currentClass = rpgClass)
    }

    override fun onConfirmClick() {
        when (val choice = state.currentClass.serverName) {
            "back" -> {}
            "optOut" -> viewModelScope.launchCatching {
                userRepository.disableClasses()
            }
            else -> viewModelScope.launchCatching {
                userRepository.changeClass(choice)}
        }
        state = state.copy(shouldNavigateBack = true)
    }

    fun onAnyClk(classSelectionCargo: ClassSelectionCargo) {
        classSelectionCargo.unpack(this)
    }
}