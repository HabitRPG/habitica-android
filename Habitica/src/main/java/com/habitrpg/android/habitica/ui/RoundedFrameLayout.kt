package com.habitrpg.android.habitica.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.toRectF

class RoundedFrameLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        var radius = 4f
        var isCirclular = true
        val clipPath = Path()
        val bounds = Rect()

        override fun onDraw(canvas: Canvas) {
            clipPath.reset()
            val radius = if (isCirclular) (height / 2).toFloat() else radius
            canvas.getClipBounds(bounds)
            clipPath.addRoundRect(bounds.toRectF(), radius, radius, Path.Direction.CW)
            canvas.clipPath(clipPath)
            super.onDraw(canvas)
        }
    }
