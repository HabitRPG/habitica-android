package com.habitrpg.android.habitica.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.HabitResetOption
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskFormViewModel
@Inject
constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel
) : BaseViewModel(userRepository, userViewModel) {
    val taskDifficulty = mutableStateOf(TaskDifficulty.EASY)
    val selectedAttribute = mutableStateOf(Attribute.STRENGTH)
    val habitResetOption = mutableStateOf(HabitResetOption.DAILY)
    val habitScoringPositive = mutableStateOf(true)
    val habitScoringNegative = mutableStateOf(false)
}
