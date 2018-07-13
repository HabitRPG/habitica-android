package com.habitrpg.android.habitica.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout


class RoundedFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var radius = 4f
    var isCirclular = true

    override fun onDraw(canvas: Canvas) {
        val clipPath = Path()
        val radius = if (isCirclular) (canvas.height/2).toFloat() else radius
        clipPath.addRoundRect(RectF(canvas.clipBounds), radius, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }
}