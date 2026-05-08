package com.habitrpg.android.habitica.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.views.AvatarView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull

class AvatarWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        if (appWidgetIds.isEmpty()) return
        render(context, appWidgetManager, appWidgetIds)
    }

    private fun render(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        MainScope().launchCatching {
            val user = widgetEntryPoint(context.applicationContext)
                .userRepository()
                .getUser()
                .firstOrNull() ?: return@launchCatching

            val avatarView = AvatarView(
                context.applicationContext,
                showBackground = true,
                showMount = true,
                showPet = true,
            )
            avatarView.setAvatar(user)
            avatarView.onAvatarImageReady { bitmap ->
                bitmap ?: return@onAvatarImageReady
                val filled = zoomBitmap(bitmap, CONTENT_FILL_SCALE)
                val openApp = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context.applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                appWidgetIds.forEach { id ->
                    val views = RemoteViews(context.packageName, R.layout.widget_avatar)
                    views.setImageViewBitmap(R.id.avatar_image, filled)
                    views.setOnClickPendingIntent(R.id.avatar_root, openApp)
                    appWidgetManager.updateAppWidget(id, views)
                }
            }
        }
    }

    private fun zoomBitmap(src: Bitmap, scale: Float): Bitmap {
        val out = createBitmap(src.width, src.height)
        val canvas = Canvas(out)
        val matrix = Matrix().apply {
            postScale(scale, scale, src.width / 2f, src.height / 2f)
        }
        val paint = Paint().apply { isFilterBitmap = false }
        canvas.drawBitmap(src, matrix, paint)
        return out
    }

    companion object {
        private const val CONTENT_FILL_SCALE = 1.06f

        fun renderAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, AvatarWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            AvatarWidgetProvider().onUpdate(context, manager, ids)
        }
    }
}
