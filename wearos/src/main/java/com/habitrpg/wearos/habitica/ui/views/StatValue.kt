package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.views.HabiticaIconsHelper.init

class StatValue : FrameLayout {

    var view: View? = null
    var bitmapImageView: ImageView? = null
    var currentValueTextView: TextView? = null
    var maxValueTextView: TextView? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    init {
        initView()
    }

    private fun initView() {
        view = inflate(context, R.layout.stat_value_layout, this)
        bitmapImageView = view?.findViewById(R.id.bitmap)
        currentValueTextView = view?.findViewById(R.id.current_value)
        maxValueTextView = view?.findViewById(R.id.max_value)
    }

    fun setStatValue(maxValue: Int, currentValue: Int, bitmap: Bitmap, bitmapColor: Int) {
        bitmapImageView?.setImageBitmap(bitmap)
        currentValueTextView?.text = currentValue.toString()
        currentValueTextView?.setTextColor(
            context?.resources?.getColor(bitmapColor, null) ?: Color.WHITE
        )
        maxValueTextView?.text = "/$maxValue"
        view?.invalidate()
    }


}