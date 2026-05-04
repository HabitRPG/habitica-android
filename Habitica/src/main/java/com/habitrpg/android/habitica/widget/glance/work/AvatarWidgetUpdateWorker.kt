package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import kotlinx.coroutines.flow.firstOrNull

class AvatarWidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val user = widgetEntryPoint(ctx).userRepository().getUser().firstOrNull()
        AvatarBitmapCache.refreshIfNeeded(ctx, user)
        AvatarWidgetRenderer.pushUpdate(ctx)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "avatar_widget_update"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<AvatarWidgetUpdateWorker>()
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
