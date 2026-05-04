package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.habitrpg.android.habitica.widget.glance.work.AvatarWidgetUpdateWorker

class AvatarWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        AvatarWidgetUpdateWorker.enqueue(context)
    }
}
