package com.habitrpg.android.habitica.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskAlarmBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        HabiticaBaseApplication.userComponent?.inject(this)
        MainScope().launch(ExceptionHandler.coroutine()) {
            taskAlarmManager.scheduleAllSavedAlarms(sharedPreferences.getBoolean("preventDailyReminder", false))
        }
        HLogger.log(LogLevel.INFO, this::javaClass.name, "onReceive")
    }
}
