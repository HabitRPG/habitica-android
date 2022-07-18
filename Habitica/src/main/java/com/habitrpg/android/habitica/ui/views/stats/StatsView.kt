package com.habitrpg.android.habitica.ui.views.stats

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.StatsViewBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class StatsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val binding = StatsViewBinding.inflate(context.layoutInflater, this, true)

    var levelValue: Int = 0
        set(value) {
            field = value
            binding.levelValueTextView.text = value.toString()
        }
    var equipmentValue: Int = 0
        set(value) {
            field = value
            binding.equipmentValueTextView.text = value.toString()
        }
    var buffValue: Int = 0
        set(value) {
            field = value
            binding.buffValueTextView.text = value.toString()
        }
    var allocatedValue: Int = 0
        set(value) {
            field = value
            binding.allocatedValueTextView.text = value.toString()
        }

    var totalValue: Int = 0
        set(value) {
            field = value
            binding.totalValueTextView.text = value.toString()
        }

    var canDistributePoints: Boolean = false
        set(value) {
            field = value
            binding.allocateButton.visibility = if (value) View.VISIBLE else View.GONE
            if (value) {
                binding.allocatedWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background_30))
                binding.allocateButton.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background_30))
                binding.allocatedValueTextView.setTextColor(statColor)
                binding.allocatedLabelView.setTextColor(statColor)
            } else {
                binding.allocatedWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.window_background))
                binding.allocateButton.setBackgroundColor(ContextCompat.getColor(context, R.color.window_background))
                binding.allocatedValueTextView.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                binding.allocatedLabelView.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
            }
        }

    var allocateAction: (() -> Unit)? = null

    private var statColor: Int = 0

    init {
        val attributes = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.StatsView,
            0, 0
        )

        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.layout_top_rounded_bg_white)
        if (attributes != null) {
            statColor = attributes.getColor(R.styleable.StatsView_statsColor, 0)
            backgroundDrawable?.setTintWith(attributes.getColor(R.styleable.StatsView_titleBackgroundColor, 0), PorterDuff.Mode.MULTIPLY)
            binding.titleTextView.text = attributes.getString(R.styleable.StatsView_statsTitle)
        }
        binding.titleWrapper.background = backgroundDrawable

        binding.allocateButton.setOnClickListener {
            HapticFeedbackManager.tap(this)
            allocateAction?.invoke()
        }

        binding.allocateButton.setImageBitmap(HabiticaIconsHelper.imageOfAttributeAllocateButton())
    }
}
