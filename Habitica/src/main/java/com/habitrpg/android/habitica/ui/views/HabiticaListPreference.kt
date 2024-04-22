package com.habitrpg.android.habitica.ui.views

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.ListPreference
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.setScaledPadding
import kotlin.math.max

class HabiticaListPreference : ListPreference {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun onClick() {
        val subtitleText = TextView(context)
        subtitleText.setText(R.string.cds_subtitle)
        val builder =
            AlertDialog.Builder(context)
                .setSingleChoiceItems(entries, getValueIndex() + 1) { dialog, index ->
                    val actualIndex = max(0, index - 1)
                    setValueIndex(actualIndex)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setTitle(title)

        val dialog = builder.create()
        subtitleText.setScaledPadding(context, 24, 0, 24, 8)
        dialog.listView.addHeaderView(subtitleText)
        dialog.window?.decorView?.setBackgroundResource(R.color.window_background)
        dialog.show()
    }

    private fun getValueIndex() = entryValues.indexOf(value)
}
