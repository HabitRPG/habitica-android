package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensionsCommon.isUsingNightModeResources

class DayNightTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    init {
        val attributes =
            context.theme?.obtainStyledAttributes(attrs, R.styleable.DayNightTextView, 0, 0)
        setTextColor(
            if (context.isUsingNightModeResources()) {
                attributes?.getColor(
                    R.styleable.DayNightTextView_nightTextColor,
                    ContextCompat.getColor(context, R.color.text_primary),
                )
            } else {
                attributes?.getColor(
                    R.styleable.DayNightTextView_dayTextColor,
                    ContextCompat.getColor(context, R.color.text_primary),
                )
            } ?: ContextCompat.getColor(context, R.color.text_primary),
        )
    }
}
