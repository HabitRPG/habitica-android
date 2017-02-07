package com.habitrpg.android.habitica.ui.fragments.preferences;


import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.prefs.TimePreference;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DayStartPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
    public static final String TAG = TimePreferenceDialogFragment.class.getSimpleName();

    private TimePicker picker = null;
    private TextView descriptionTextView;

    public DayStartPreferenceDialogFragment() {
    }

    public static DayStartPreferenceDialogFragment newInstance(
            PreferenceFragmentCompat preferenceFragment, String key) {
        DayStartPreferenceDialogFragment fragment = new DayStartPreferenceDialogFragment();
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
        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        picker = new TimePicker(context);
        descriptionTextView = new TextView(context);
        descriptionTextView.setTextColor(getResources().getColor(R.color.textColorLight));
        int padding = (int) getResources().getDimension(R.dimen.card_padding);
        descriptionTextView.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        wrapper.addView(picker, lp);
        wrapper.addView(descriptionTextView, lp);
        picker.setOnTimeChangedListener((timePicker, i, i1) -> {
            updateDescriptionText(i);
        });
        return wrapper;
    }

    private void updateDescriptionText(int hour) {
        Calendar date = new GregorianCalendar();
        if (date.get(Calendar.HOUR) < hour) {
            date.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH) + 1);
        }
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        DateFormat dateFormatter = DateFormat.getDateTimeInstance();
        descriptionTextView.setText(getString(R.string.cds_description, dateFormatter.format(date.getTime())));
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TimePreference preference = getTimePreference();
        int lastHour = preference.getLastHour();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker.setHour(lastHour);
            picker.setMinute(0);
        } else {
            picker.setCurrentHour(lastHour);
            picker.setCurrentMinute(0);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lastHour = picker.getHour();
        } else {
            lastHour = picker.getCurrentHour();
        }
        return String.valueOf(lastHour) + ":00";
    }
}
