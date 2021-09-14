package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.prefs.TimePreference
import java.text.DateFormat
import java.util.*

class DayStartPreferenceDialogFragment : PreferenceDialogFragmentCompat() {

    private var picker: TimePicker? = null
    private var descriptionTextView: TextView? = null

    private val timePreference: TimePreference
        get() = preference as TimePreference

    private val newTimeValue: String
        get() {
            val lastHour: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                picker?.hour
            } else {
                @Suppress("DEPRECATION")
                picker?.currentHour
            }
            return lastHour.toString() + ":00"
        }

    override fun onCreateDialogView(context: Context?): View {
        val wrapper = LinearLayout(context)
        wrapper.orientation = LinearLayout.VERTICAL
        picker = TimePicker(context)
        descriptionTextView = TextView(context)
        @Suppress("DEPRECATION")
        descriptionTextView?.setTextColor(resources.getColor(R.color.text_primary))
        val padding = resources.getDimension(R.dimen.card_padding).toInt()
        descriptionTextView?.setPadding(padding, padding, padding, padding)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        wrapper.addView(picker, lp)
        wrapper.addView(descriptionTextView, lp)
        picker?.setOnTimeChangedListener { _, i, _ -> updateDescriptionText(i) }
        return wrapper
    }

    private fun updateDescriptionText(hour: Int) {
        val date = GregorianCalendar()
        if (date.get(Calendar.HOUR) >= hour) {
            date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH) + 1)
        }
        date.set(Calendar.HOUR_OF_DAY, hour)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        val dateFormatter = DateFormat.getDateTimeInstance()
        descriptionTextView?.text = getString(R.string.cds_description, dateFormatter.format(date.time))
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val preference = timePreference
        val lastHour = preference.lastHour
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker?.hour = lastHour
            picker?.minute = 0
        } else {
            @Suppress("DEPRECATION")
            picker?.currentHour = lastHour
            @Suppress("DEPRECATION")
            picker?.currentMinute = 0
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val preference = timePreference
            val time = newTimeValue

            preference.summary = time

            if (preference.callChangeListener(time)) {
                preference.text = time
            }
        }
    }

    companion object {
        val TAG: String? = TimePreferenceDialogFragment::class.java.simpleName

        fun newInstance(
            preferenceFragment: PreferenceFragmentCompat,
            key: String
        ): DayStartPreferenceDialogFragment {
            val fragment = DayStartPreferenceDialogFragment()
            val arguments = Bundle(1)
            arguments.putString(ARG_KEY, key)
            fragment.arguments = arguments
            fragment.setTargetFragment(preferenceFragment, 0)
            return fragment
        }
    }
}
