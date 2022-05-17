package com.habitrpg.common.habitica.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import java.lang.Integer.min

class PixelArtView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private var targetRect = Rect(0, 0, 0, 0)

    var bitmap: Bitmap? = null
        set(value) {
            field = value
            updateTargetRect()
            invalidate()
        }

    private val paint: Paint by lazy {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = false
        paint
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTargetRect()
    }

    private fun updateTargetRect() {
        var targetWidth = bitmap?.width ?: 0
        var targetHeight = bitmap?.height ?: 0
        val smallestSide = min(width, height)

        val factor = min(
            (if (smallestSide > 0 && targetWidth > 0 && smallestSide != targetWidth) {
                smallestSide / (targetWidth / 3)
            } else 1),
            if (smallestSide > 0 && targetHeight > 0 && smallestSide != targetHeight) {
                smallestSide / (targetHeight / 3)
            } else 1
        )
        targetWidth = (targetWidth / 3) * factor
        targetHeight = (targetHeight / 3) * factor
        val left = (width - targetWidth) / 2
        val top = (height - targetHeight) / 2
        targetRect = Rect(left, top, left + targetWidth, top + targetHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        if (bitmap == null) {
            super.onDraw(canvas)
            return
        }
        val bitmap = bitmap ?: return
        canvas?.drawBitmap(bitmap, null, targetRect, paint)
    }
}
