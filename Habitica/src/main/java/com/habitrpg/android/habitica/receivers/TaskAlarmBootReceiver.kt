package com.habitrpg.android.habitica.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskAlarmBootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) {
            return
        }
        MainScope().launch(ExceptionHandler.coroutine()) {
            taskAlarmManager.scheduleAllSavedAlarms(
                sharedPreferences.getBoolean(
                    "preventDailyReminder",
                    false
                )
            )
        }
        HLogger.log(LogLevel.INFO, this::javaClass.name, "onReceive")
    }
}
