package com.habitrpg.android.habitica.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

//
public class TimePreference extends DialogPreference {
    private int lastHour = 0;
    private int lastMinute = 0;
    private String timeval;
    private CharSequence mSummary;
    private TimePicker picker = null;
    public static int getHour(String timeval) {
        String[] pieces = timeval.split(":");
        return (Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String timeval) {
        String[] pieces = timeval.split(":");
        return (Integer.parseInt(pieces[1]));
    }


    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker.setHour(lastHour);
            picker.setMinute(lastMinute);
        } else {
            picker.setCurrentHour(lastHour);
            picker.setCurrentMinute(lastMinute);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lastHour = picker.getHour();
                lastMinute = picker.getMinute();
            } else {
                lastHour = picker.getCurrentHour();
                lastMinute = picker.getCurrentMinute();
            }
            String timeval = String.valueOf(lastHour) + ":" + String.format("%02d", lastMinute);

            this.setSummary(timeval);

            if (callChangeListener(timeval)) {
                persistString(timeval);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        timeval = null;

        if (restoreValue) {
            if (defaultValue == null) {
                timeval = getPersistedString("19:00");
            } else {
                timeval = getPersistedString(defaultValue.toString());
            }
        } else {
            timeval = defaultValue.toString();
        }
        setSummary(timeval);
        lastHour = getHour(timeval);
        lastMinute = getMinute(timeval);
    }

    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        timeval = text;

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public String getText() {
        return timeval;
    }
}