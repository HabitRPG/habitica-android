package com.habitrpg.android.habitica.prefs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.habitrpg.common.habitica.helpers.LanguageHelper
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

class TimePreference(ctxt: Context, attrs: AttributeSet?) : DialogPreference(ctxt, attrs) {
    private var timeval: String? = null

    override fun onGetDefaultValue(
        a: TypedArray,
        index: Int
    ): Any {
        return a.getString(index)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        timeval = null
        timeval =
            if (defaultValue == null) {
                getPersistedString("19:00")
            } else {
                getPersistedString(defaultValue.toString())
            }
        summary = timeval ?: ""
    }

    val lastHour: Int
        get() = getHour(timeval)
    val lastMinute: Int
        get() = getMinute(timeval)
    var text: String?
        get() = timeval
        set(text) {
            val wasBlocking = shouldDisableDependents()
            timeval = text
            persistString(text)
            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking)
            }
        }

    override fun setSummary(summary: CharSequence?) {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(Calendar.HOUR_OF_DAY, getHour(timeval))
        calendar.set(Calendar.MINUTE, getMinute(timeval))
        val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, LanguageHelper.systemLocale)
        super.setSummary(formatter.format(calendar.time))
    }

    companion object {
        fun getHour(timeval: String?): Int {
            return timeval?.split(":")?.get(0)?.toInt() ?: 0
        }

        fun getMinute(timeval: String?): Int {
            return timeval?.split(":")?.get(1)?.toInt() ?: 0
        }
    }

    init {
        positiveButtonText = "Set"
        negativeButtonText = "Cancel"
    }
}
