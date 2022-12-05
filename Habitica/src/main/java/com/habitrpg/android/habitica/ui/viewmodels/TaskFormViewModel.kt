package com.habitrpg.android.habitica.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.HabitResetOption
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty

class TaskFormViewModel: BaseViewModel() {
    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    val taskDifficulty = mutableStateOf(TaskDifficulty.EASY)
    val selectedAttribute = mutableStateOf(Attribute.STRENGTH)
    val habitResetOption = mutableStateOf(HabitResetOption.DAILY)
    val habitScoringPositive = mutableStateOf(true)
    val habitScoringNegative = mutableStateOf(false)
}
