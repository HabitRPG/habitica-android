package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskSingleGlanceWidget

class AddTaskRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        runCatching {
            AddTaskSingleGlanceWidget().updateAll(applicationContext)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "add_task_widget_refresh"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<AddTaskRefreshWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
