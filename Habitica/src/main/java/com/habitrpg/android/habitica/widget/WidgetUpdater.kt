package com.habitrpg.android.habitica.widget

import android.content.Context
import com.habitrpg.android.habitica.widget.glance.work.WidgetRefreshWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    internal constructor(
        context: Context,
        refreshScheduler: (Context) -> Unit
    ) : this(context) {
        this.refreshScheduler = refreshScheduler
    }

    private var refreshScheduler: (Context) -> Unit = WidgetRefreshWorker::enqueueOneTime

    fun updateAllWidgets() {
        refreshScheduler(context)
    }

    fun updateTaskListWidgets() {
        refreshScheduler(context)
    }
}
