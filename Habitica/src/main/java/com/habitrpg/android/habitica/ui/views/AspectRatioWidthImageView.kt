package com.habitrpg.android.habitica.ui.views

class AspectRatioWidthImageView: androidx.appcompat.widget.AppCompatImageView {

    constructor(context: android.content.Context) : super(context)
    constructor(context: android.content.Context, attrs: android.util.AttributeSet?) : super(context, attrs)
    constructor(context: android.content.Context, attrs: android.util.AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width * drawable.intrinsicHeight / drawable.intrinsicWidth
        setMeasuredDimension(width, height)
    }

}