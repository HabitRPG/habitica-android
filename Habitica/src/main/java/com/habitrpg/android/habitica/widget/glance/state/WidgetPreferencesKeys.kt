package com.habitrpg.android.habitica.widget.glance.state

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
    val refreshToken = intPreferencesKey("refresh_token")
    val statOverrideValid = booleanPreferencesKey("stat_override_valid")
    val statOverrideHp = floatPreferencesKey("stat_override_hp")
    val statOverrideExp = floatPreferencesKey("stat_override_exp")
    val statOverrideMp = floatPreferencesKey("stat_override_mp")
    val statOverrideGold = doublePreferencesKey("stat_override_gold")
}
