package com.habitrpg.android.habitica.widget.glance.work

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
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
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.widget.AvatarWidgetProvider
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.common.habitica.extensions.dpToPx

object AvatarWidgetRenderer {
    private const val TILE_DP = 120
    private const val CORNER_RADIUS_DP = 20

    fun pushUpdate(context: Context) {
        val cached = AvatarBitmapCache.cachedBitmap(context) ?: return
        val sizePx = TILE_DP.dpToPx(context)
        val radiusPx = CORNER_RADIUS_DP.dpToPx(context).toFloat()
        val composed = composeAvatar(cached, sizePx, radiusPx)
        val views = buildViews(context, composed)
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, AvatarWidgetProvider::class.java)
        manager.getAppWidgetIds(component).forEach { id ->
            manager.updateAppWidget(id, views)
        }
    }

    private fun buildViews(context: Context, bitmap: Bitmap): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_avatar)
        views.setImageViewBitmap(R.id.avatar_image, bitmap)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        views.setOnClickPendingIntent(R.id.avatar_root, pendingIntent)
        return views
    }

    private fun composeAvatar(src: Bitmap, sizePx: Int, radiusPx: Float): Bitmap {
        val out = createBitmap(sizePx, sizePx)
        val canvas = Canvas(out)
        val clipPath = Path().apply {
            addRoundRect(
                RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()),
                radiusPx,
                radiusPx,
                Path.Direction.CW,
            )
        }
        canvas.clipPath(clipPath)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val drawW: Int
        val drawH: Int
        if (srcRatio >= 1f) {
            drawW = sizePx
            drawH = (sizePx / srcRatio).toInt()
        } else {
            drawW = (sizePx * srcRatio).toInt()
            drawH = sizePx
        }
        val xOffset = (sizePx - drawW) / 2
        val yOffset = (sizePx - drawH) / 2
        val srcRect = Rect(0, 0, src.width, src.height)
        val dstRect = Rect(xOffset, yOffset, xOffset + drawW, yOffset + drawH)
        canvas.drawBitmap(src, srcRect, dstRect, paint)
        return out
    }
}
