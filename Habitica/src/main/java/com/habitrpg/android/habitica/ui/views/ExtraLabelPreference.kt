package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.habitrpg.android.habitica.R

class ExtraLabelPreference(
    context: Context,
    attrs: AttributeSet?
) : Preference(context, attrs) {
    var extraText: String? = null
    var extraTextColor: Int? = null

    init {
        layoutResource = R.layout.preference_button
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val textView = holder.itemView.findViewById<TextView>(R.id.extra_label)
        textView?.text = extraText
        extraTextColor?.let {
            textView?.setTextColor(it)
        }
    }
}
