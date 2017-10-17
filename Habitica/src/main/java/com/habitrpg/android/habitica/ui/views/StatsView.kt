package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import kotlinx.android.synthetic.main.stats_view.view.*

class StatsView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var levelValue: Int = 0
        set(value) {
            levelValueTextView.text = value.toString()
        }
    var equipmentValue: Int = 0
        set(value) {
            equipmentValueTextView.text = value.toString()
        }
    var buffValue: Int = 0
        set(value) {
            buffValueTextView.text = value.toString()
        }
    var allocatedValue: Int = 0
        set(value) {
            allocatedValueTextView.text = value.toString()
        }

    var totalValue: Int = 0
        set(value) {
            totalValueTextView.text = value.toString()
        }

    init {
        View.inflate(context, R.layout.stats_view, this)

        val attributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.StatsView,
                0, 0)

        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.layout_top_rounded_bg)
        if (attributes != null) {
            backgroundDrawable.setColorFilter(attributes.getColor(R.styleable.StatsView_titleBackgroundColor, 0), PorterDuff.Mode.MULTIPLY)
            titleTextView.text = attributes.getString(R.styleable.StatsView_statsTitle)
        }
        titleWrapper.background = backgroundDrawable
    }

}