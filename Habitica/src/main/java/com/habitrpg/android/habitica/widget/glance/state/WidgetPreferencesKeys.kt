package com.habitrpg.android.habitica.widget.glance.state

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.action.ActionParameters

object WidgetActionKeys {
    val taskId = ActionParameters.Key<String>("task_id")
    val direction = ActionParameters.Key<String>("direction")
    val deepLink = ActionParameters.Key<String>("deep_link")
}

object WidgetStateKeys {
    val addTaskSingleType = stringPreferencesKey("add_task_single_type")
    val habitButtonTaskId = stringPreferencesKey("habit_button_task_id")
    val taskListHiddenIds = stringSetPreferencesKey("task_list_hidden_ids")
}
