package com.habitrpg.android.habitica.ui.views.stats

import android.content.Context
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import kotlinx.android.synthetic.main.stats_view.view.*

class StatsView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var levelValue: Int = 0
        set(value) {
            field = value
            levelValueTextView.text = value.toString()
        }
    var equipmentValue: Int = 0
        set(value) {
            field = value
            equipmentValueTextView.text = value.toString()
        }
    var buffValue: Int = 0
        set(value) {
            field = value
            buffValueTextView.text = value.toString()
        }
    var allocatedValue: Int = 0
        set(value) {
            field = value
            allocatedValueTextView.text = value.toString()
        }

    var totalValue: Int = 0
        set(value) {
            field = value
            totalValueTextView.text = value.toString()
        }

    var canDistributePoints: Boolean = false
        set(value) {
            field = value
            allocateButton.visibility = if (value) View.VISIBLE else View.GONE
            if (value) {
                allocatedWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_600_30))
                allocateButton.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_600_30))
                allocatedValueTextView.setTextColor(statColor)
                allocatedLabelView.setTextColor(statColor)
            } else {
                allocatedWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_700))
                allocateButton.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_700))
                allocatedValueTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_50))
                allocatedLabelView.setTextColor(ContextCompat.getColor(context, R.color.gray_300))
            }
        }

    var allocateAction: (() -> Unit)? = null

    private var statColor: Int = 0

    init {
        View.inflate(context, R.layout.stats_view, this)

        val attributes = context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.StatsView,
                0, 0)

        if (context != null) {
            val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.layout_top_rounded_bg)
            if (attributes != null) {
                statColor = attributes.getColor(R.styleable.StatsView_statsColor, 0)
                backgroundDrawable?.setColorFilter(attributes.getColor(R.styleable.StatsView_titleBackgroundColor, 0), PorterDuff.Mode.MULTIPLY)
                titleTextView.text = attributes.getString(R.styleable.StatsView_statsTitle)
            }
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                @Suppress("DEPRECATION")
                titleWrapper.setBackgroundDrawable(backgroundDrawable)
            } else {
                titleWrapper.background = backgroundDrawable
            }
        }

        allocateButton.setOnClickListener {
            allocateAction?.invoke()
        }

        allocateButton.setImageBitmap(HabiticaIconsHelper.imageOfAttributeAllocateButton())
    }
}