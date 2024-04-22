package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ButtonAddTaskBinding
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater

class AddTaskButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : FrameLayout(context, attrs) {
        val binding = ButtonAddTaskBinding.inflate(context.layoutInflater, this)

        private var gradient =
            LinearGradient(
                0f,
                0f,
                0f,
                80.dpToPx(context).toFloat(),
                ContextCompat.getColor(context, R.color.watch_purple_100),
                ContextCompat.getColor(context, R.color.watch_blue_100),
                Shader.TileMode.CLAMP,
            )

        private val fillPaint =
            Paint().apply {
                style = Paint.Style.FILL_AND_STROKE
                shader = gradient
                isAntiAlias = true
            }
        private val strokePaint =
            Paint().apply {
                style = Paint.Style.STROKE
                color = ContextCompat.getColor(context, R.color.watch_purple_200)
                strokeWidth = 3f.dpToPx(context)
                isAntiAlias = true
            }
        private val path = Path()
        private val rect = RectF(0f, 1.5f.dpToPx(context), 0f, 0f)

        init {
            setWillNotDraw(false)
            clipToOutline = false
            clipChildren = false
        }

        override fun onLayout(
            changed: Boolean,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
        ) {
            super.onLayout(changed, left, top, right, bottom)
            val totalWidth = right - left
            val width = totalWidth / 1.2375f
            rect.left = (totalWidth - width) / 2
            rect.right = rect.left + width
            rect.bottom = width / 1.8f
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            path.reset()
            path.addArc(rect, 180f, 360f)
            canvas.drawPath(path, fillPaint)
            canvas.drawPath(path, strokePaint)
        }
    }
