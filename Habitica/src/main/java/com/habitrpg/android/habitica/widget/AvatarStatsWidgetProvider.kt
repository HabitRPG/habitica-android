package com.habitrpg.android.habitica.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.ui.activities.MainActivity

class AvatarStatsWidgetProvider : BaseWidgetProvider() {

    private var appWidgetManager: AppWidgetManager? = null

    override fun layoutResourceId(): Int {
        return R.layout.widget_main_avatar_stats
    }

    private fun setUp() {
        if (!hasInjected) {
            hasInjected = true
            HabiticaBaseApplication.userComponent?.inject(this)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        this.setUp()
        this.appWidgetManager = appWidgetManager
        this.context = context
        for (widgetId in appWidgetIds) {

            val intent = Intent(context, AvatarStatsWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val openAppIntent = Intent(context.applicationContext, MainActivity::class.java)
            val openApp = PendingIntent.getActivity(context, 0, openAppIntent, withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT))

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_main_avatar_stats)
            remoteViews.setRemoteAdapter(R.id.widget_avatar_list, intent)
            remoteViews.setEmptyView(R.id.widget_avatar_list, R.id.widget_avatar_empty_view)
            remoteViews.setOnClickPendingIntent(R.id.widget_main_avatar_view, openApp)
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_avatar_list)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_avatar_list)
    }

    override fun configureRemoteViews(remoteViews: RemoteViews, widgetId: Int, columns: Int, rows: Int): RemoteViews = remoteViews
}
