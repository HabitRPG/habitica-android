package com.habitrpg.android.habitica.ui.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

/**
 * Simple utilities class for UI related stuff.
 */
public class UiUtils {

    /**
     * Hides soft keyboard if it's opened.
     *
     * @param activity Currently visible activity.
     */
    public static void dismissKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * Hides soft keyboard if it's opened.
     * This eliminates weird behavior when hiding keyboard from within Dialog
     *
     * @param view     View that currently has focus
     * @param activity - Current activity
     */
    public static void dismissKeyboard(Activity activity, @Nullable View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
