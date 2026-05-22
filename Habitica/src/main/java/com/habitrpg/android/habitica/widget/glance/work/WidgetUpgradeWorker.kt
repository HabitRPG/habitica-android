package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.glance.migration.LegacyWidgetMigration

class WidgetUpgradeWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        LegacyWidgetMigration.runIfNeeded(applicationContext)
        WidgetRefreshWorker.refreshAllWidgetsNow(applicationContext)
        return Result.success()
    }
}
