package com.habitrpg.android.habitica.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.preference.DialogPreference;
import android.util.AttributeSet;

public class TimePreference extends DialogPreference {
    private String timeval;

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    public static int getHour(String timeval) {
        String[] pieces = timeval.split(":");
        return (Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String timeval) {
        String[] pieces = timeval.split(":");
        return (Integer.parseInt(pieces[1]));
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
    }

    public int getLastHour() {
        return getHour(timeval);
    }

    public int getLastMinute() {
        return getMinute(timeval);
    }

    public String getText() {
        return timeval;
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
}