package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SupportCollapsibleSectionBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.MarkdownParser

class SupportCollapsibleSection : LinearLayout {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val binding = SupportCollapsibleSectionBinding.inflate(context.layoutInflater, this)
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.SupportCollapsibleSection,
            defStyle,
            0
        )

        orientation = VERTICAL

        binding.titleView.text = a.getString(R.styleable.SupportCollapsibleSection_title)
        binding.subtitleView.text = a.getString(R.styleable.SupportCollapsibleSection_subtitle)
        binding.descriptionView.text = MarkdownParser.parseMarkdown(a.getString(R.styleable.SupportCollapsibleSection_description))
        binding.titleView.setTextColor(a.getColor(R.styleable.SupportCollapsibleSection_titleColor, ContextCompat.getColor(context, R.color.text_primary)))

        background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_window)

        a.recycle()

        setOnClickListener {
            val shouldBeVisible = binding.descriptionView.visibility != View.VISIBLE
            if (shouldBeVisible) {
                binding.caretView.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            } else {
                binding.caretView.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }
            val descriptionIndex = children.indexOf(binding.descriptionView)
            children.forEachIndexed { index, view ->
                if (index >= descriptionIndex) {
                    view.isVisible = shouldBeVisible
                }
            }
        }
    }
}
