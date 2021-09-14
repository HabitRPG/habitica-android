package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.*
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R

class AdventureGuideDrawerArrowDrawable(context: Context?) : DrawerArrowDrawable(context) {
    private val backgroundPaint: Paint = Paint()
    private val icon: Bitmap = BitmapFactory.decodeResource(context?.resources, R.drawable.star)
    private var enabled = true
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!enabled) {
            return
        }
        val bounds = bounds
        val x = bounds.width() - HALF_SIZE
        val y = 10f
        canvas.drawCircle(x, y, SIZE, backgroundPaint)
        canvas.drawBitmap(icon, x - 16, y - 16, null)
    }

    fun setEnabled(enabled: Boolean) {
        if (this.enabled != enabled) {
            this.enabled = enabled
            invalidateSelf()
        }
    }

    fun isEnabled(): Boolean {
        return enabled
    }

    companion object {
        // Fraction of the drawable's intrinsic size we want the badge to be.
        private const val SIZE = 24f
        private const val HALF_SIZE = SIZE / 2
    }

    init {
        context?.let { backgroundPaint.color = ContextCompat.getColor(it, R.color.yellow_10) }
        backgroundPaint.isAntiAlias = true
    }
}
