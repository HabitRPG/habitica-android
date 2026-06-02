package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class CronBoundaryRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!WidgetAuth.isLoggedIn(context) || !hasDailyWidgets(context)) {
            return Result.success()
        }

        val user = try {
            widgetEntryPoint(context).userRepository().retrieveUser(withTasks = true, forced = true)
        } catch (e: Exception) {
            ExceptionHandler.reportError(e)
            return Result.retry()
        }

        WidgetRefreshWorker.clearTaskListHiddenIds(context)
        WidgetRefreshWorker.refreshAllWidgetsNow(context)

        schedule(context, user?.preferences?.dayStart ?: 0)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "habitica_cron_boundary_refresh"

        private const val BUFFER_MINUTES = 2

        internal fun nextBoundaryMillis(dayStart: Int, now: Long): Long {
            val hour = dayStart.coerceIn(0, 24) % 24
            val cal = Calendar.getInstance()
            cal.timeInMillis = now
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MINUTE, BUFFER_MINUTES)
            if (cal.timeInMillis <= now) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        private suspend fun hasDailyWidgets(context: Context): Boolean {
            val manager = GlanceAppWidgetManager(context)
            return manager.getGlanceIds(DailyTaskListGlanceWidget::class.java).isNotEmpty() ||
                manager.getGlanceIds(DailiesCountGlanceWidget::class.java).isNotEmpty()
        }

        suspend fun scheduleFromCache(context: Context) {
            if (!WidgetAuth.isLoggedIn(context) || !hasDailyWidgets(context)) {
                cancel(context)
                return
            }
            val user = withContext(Dispatchers.Main) {
                widgetEntryPoint(context).userRepository().getUser().firstOrNull()
            }
            schedule(context, user?.preferences?.dayStart ?: 0)
        }

        fun scheduleFromCacheAsync(context: Context) {
            val appContext = context.applicationContext
            MainScope().launch(ExceptionHandler.coroutine()) {
                scheduleFromCache(appContext)
            }
        }

        fun schedule(context: Context, dayStart: Int) {
            val now = Date().time
            val delay = (nextBoundaryMillis(dayStart, now) - now).coerceAtLeast(0)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<CronBoundaryRefreshWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
