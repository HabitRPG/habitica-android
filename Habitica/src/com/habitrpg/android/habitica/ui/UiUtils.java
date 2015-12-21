package com.habitrpg.android.habitica.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

}
