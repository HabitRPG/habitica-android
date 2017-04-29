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

    /**
     * Shows snackbar in given container.
     *
     * @param context   Context.
     * @param container Parent view where Snackbar will appear.
     * @param content   message.
     */
    public static void showSnackbar(Context context, View container, String content, SnackbarDisplayType displayType) {
        Snackbar snackbar = Snackbar.make(container, content, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        switch (displayType) {
            case FAILURE:
                snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.worse_10));
                break;
            case FAILURE_BLUE:
                snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.best_100));
                break;
            case DROP:
                TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                tv.setMaxLines(5);
                snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.best_10));
                break;
        }

        snackbar.show();
    }

    public enum SnackbarDisplayType {
        NORMAL, FAILURE, FAILURE_BLUE, DROP
    }

}
