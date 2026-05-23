package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity

fun openTaskFormAction(taskType: String): Action {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setClassName("com.habitrpg.android.habitica", TaskFormActivity::class.java.name)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra(TaskFormActivity.TASK_TYPE_KEY, taskType)
    return actionStartActivity(intent)
}
