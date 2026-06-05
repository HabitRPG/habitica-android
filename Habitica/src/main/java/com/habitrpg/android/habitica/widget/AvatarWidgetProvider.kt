package com.habitrpg.android.habitica.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
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
                val square = roundedSquare(bitmap)
                val targetIntent = if (!user.id.isNullOrEmpty()) {
                    Intent(context.applicationContext, FullProfileActivity::class.java).apply {
                        putExtra("userID", user.id)
                    }
                } else {
                    Intent(context.applicationContext, MainActivity::class.java)
                }
                val openProfile = PendingIntent.getActivity(
                    context,
                    0,
                    targetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                appWidgetIds.forEach { id ->
                    val views = RemoteViews(context.packageName, R.layout.widget_avatar)
                    views.setImageViewBitmap(R.id.avatar_image, square)
                    views.setOnClickPendingIntent(R.id.avatar_root, openProfile)
                    appWidgetManager.updateAppWidget(id, views)
                }
            }
        }
    }

    private fun roundedSquare(src: Bitmap): Bitmap {
        val side = minOf(src.width, src.height)
        val cropSide = (side / CONTENT_FILL_SCALE).toInt().coerceIn(1, side)
        val cropLeft = (src.width - cropSide) / 2
        val cropTop = (src.height - cropSide) / 2
        val srcRect = Rect(cropLeft, cropTop, cropLeft + cropSide, cropTop + cropSide)
        val output = createBitmap(side, side)
        val canvas = Canvas(output)
        val dest = RectF(0f, 0f, side.toFloat(), side.toFloat())
        val radius = side * CORNER_RADIUS_FRACTION
        canvas.clipPath(Path().apply { addRoundRect(dest, radius, radius, Path.Direction.CW) })
        canvas.drawBitmap(src, srcRect, dest, Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true })
        return output
    }

    companion object {
        private const val CONTENT_FILL_SCALE = 1.04f
        private const val CORNER_RADIUS_FRACTION = 0.17f

        fun renderAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, AvatarWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            AvatarWidgetProvider().onUpdate(context, manager, ids)
        }
    }
}
