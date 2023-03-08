package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.habitrpg.android.habitica.prefs.TimePreference
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimePreferenceDialogFragment : PreferenceDialogFragmentCompat() {

    lateinit var picker: TimePicker

    private val timePreference: TimePreference
        get() = preference as TimePreference

    private val newTimeValue: String
        get() {
            val lastHour: Int
            val lastMinute: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lastHour = picker.hour
                lastMinute = picker.minute
            } else {
                @Suppress("DEPRECATION")
                lastHour = picker.currentHour
                @Suppress("DEPRECATION")
                lastMinute = picker.currentMinute
            }
            return lastHour.toString() + ":" + String.format(Locale.UK, "%02d", lastMinute)
        }

    override fun onCreateDialogView(context: Context): View {
        picker = TimePicker(context)
        picker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(context))
        return picker
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val preference = timePreference
        val lastHour = preference.lastHour
        val lastMinute = preference.lastMinute
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker.hour = lastHour
            picker.minute = lastMinute
        } else {
            @Suppress("DEPRECATION")
            picker.currentHour = lastHour
            @Suppress("DEPRECATION")
            picker.currentMinute = lastMinute
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
        val TAG = TimePreferenceDialogFragment::class.java.simpleName
        fun newInstance(
            preferenceFragment: PreferenceFragmentCompat,
            key: String
        ): TimePreferenceDialogFragment {
            val fragment = TimePreferenceDialogFragment()
            val arguments = Bundle(1)
            arguments.putString(ARG_KEY, key)
            fragment.arguments = arguments
            fragment.setTargetFragment(preferenceFragment, 0)
            return fragment
        }
    }
}
