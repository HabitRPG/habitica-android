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

class AddTaskButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    val binding = ButtonAddTaskBinding.inflate(context.layoutInflater, this)

    private val paint = Paint()
    private val gradient = LinearGradient(
        0f,
        0f,
        0f,
        80.dpToPx(context).toFloat(),
        ContextCompat.getColor(context, R.color.brand_400),
        ContextCompat.getColor(context, R.color.blue_100),
        Shader.TileMode.CLAMP
    )
    private val path = Path()
    private val rect = RectF(0f, 0f, 0f, 0f)

    init {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.shader = gradient
        paint.isAntiAlias = true
        setWillNotDraw(false)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rect.right = right.toFloat()
        rect.bottom = bottom.toFloat() / 2f
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) return
        path.reset()
        path.addArc(rect, 180f, 360f)
        canvas.drawPath(path, paint)
    }
}