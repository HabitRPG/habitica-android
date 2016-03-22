package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.TimePicker;

import com.habitrpg.android.habitica.prefs.TimePreference;

import java.util.Locale;

public class TimePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    public static final String TAG = TimePreferenceDialogFragment.class.getSimpleName();

    private TimePicker picker = null;

    public TimePreferenceDialogFragment() {
    }

    public static TimePreferenceDialogFragment newInstance(
            PreferenceFragmentCompat preferenceFragment, String key) {
        TimePreferenceDialogFragment fragment = new TimePreferenceDialogFragment();
        Bundle arguments = new Bundle(1);
        arguments.putString(ARG_KEY, key);
        fragment.setArguments(arguments);
        fragment.setTargetFragment(preferenceFragment, 0);
        return fragment;
    }

    private TimePreference getTimePreference() {
        return (TimePreference) getPreference();
    }

    @Override
    protected View onCreateDialogView(Context context) {
        picker = new TimePicker(getContext());
        return picker;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TimePreference preference = getTimePreference();
        int lastHour = preference.getLastHour();
        int lastMinute = preference.getLastMinute();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker.setHour(lastHour);
            picker.setMinute(lastMinute);
        } else {
            picker.setCurrentHour(lastHour);
            picker.setCurrentMinute(lastMinute);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            TimePreference preference = getTimePreference();
            String time = getNewTimeValue();

            preference.setSummary(time);

            if (preference.callChangeListener(time)) {
                preference.setText(time);
            }
        }

    }

    private String getNewTimeValue() {
        int lastHour;
        int lastMinute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lastHour = picker.getHour();
            lastMinute = picker.getMinute();
        } else {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();
        }
        return String.valueOf(lastHour) + ":" + String.format(Locale.UK, "%02d", lastMinute);
    }
}

