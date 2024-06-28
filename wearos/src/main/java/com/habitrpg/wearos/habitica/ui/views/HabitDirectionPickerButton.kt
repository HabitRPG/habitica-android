package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.dpToPx

class HabitDirectionPickerButton
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var drawFromTop: Boolean
    private var drawable: BitmapDrawable?

    private val paint = Paint()
    private val path = Path()
    private val rect = RectF(0f, 0f, 0f, 0f)
    private val bitmapRect = RectF(0f, 0f, 0f, 0f)

    var mainTaskColor: Int = ContextCompat.getColor(context, R.color.watch_gray_200)
    var darkerTaskColor: Int = ContextCompat.getColor(context, R.color.watch_gray_10)
    var iconColor: Int = ContextCompat.getColor(context, R.color.watch_white)

    private val radius = 15.dpToPx(context)

    init {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        setWillNotDraw(false)

        val attributes =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.HabitDirectionPickerButton,
                0,
                0
            )
        drawable = attributes.getDrawable(R.styleable.HabitDirectionPickerButton_drawable) as? BitmapDrawable
        drawFromTop = attributes.getBoolean(R.styleable.HabitDirectionPickerButton_drawFromTop, false)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        rect.left = right.toFloat() * 0.125f
        rect.right = right.toFloat() * 0.875f
        if (drawFromTop) {
            rect.top = -bottom.toFloat()
            rect.bottom = bottom.toFloat()
        } else {
            rect.bottom = bottom.toFloat() / 2f
        }

        val middleX = width / 2f
        val middleY = height / 2f
        val bitmapWidthHalf = (drawable?.bitmap?.width?.toFloat() ?: 0f) / 2f
        val bitmapHeightHalf = (drawable?.bitmap?.height?.toFloat() ?: 0f) / 2f
        bitmapRect.left = middleX - bitmapWidthHalf
        bitmapRect.top = middleY - bitmapHeightHalf
        bitmapRect.right = middleX + bitmapWidthHalf
        bitmapRect.bottom = middleY + bitmapHeightHalf

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()
        if (drawFromTop) {
            path.addArc(rect, 0f, 180f)
        } else {
            path.addArc(rect, 180f, 360f)
        }
        paint.color = mainTaskColor
        canvas.drawPath(path, paint)

        val middleX = width / 2f
        val middleY = height / 2f
        paint.color = darkerTaskColor
        canvas.drawArc(middleX - radius, middleY - radius, middleX + radius, middleY + radius, 0f, 360f, true, paint)
        drawable?.let {
            paint.color = iconColor
            canvas.drawBitmap(it.bitmap, null, bitmapRect, paint)
        }
    }
}
