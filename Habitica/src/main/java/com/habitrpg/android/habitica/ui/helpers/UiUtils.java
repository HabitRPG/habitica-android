package com.habitrpg.android.habitica.ui.helpers;

import com.habitrpg.android.habitica.R;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.Date;

/**
 * Simple utilities class for UI related stuff.
 */
public class UiUtils {
    /**
     * Reference to the lastShownSnackbar.
     */
    private static Snackbar lastShownSnackbar = null;

    /**
     * Time in ms when the last snackbar was shown
     */
    private static long lastShownSnackbarTime = 0;

    /**
     * An approximation of the time between the snackbar takes to initiate animations + slide down +
     * slide up + states that is undesired. See more in: BaseTransientBottomBar.ANIMATION_DURATION,
     * BaseTransientBottomBar.ANIMATION_FADE_DURATION.
     */
    private static int APPROXIMATE_SNACKBAR_TIME = 600;

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
     * Shows snackbar in given container. Only one snackbar is shown at any given time.
     *
     * There are 3 states the snackbar is in:
     *  1. sliding up
     *  2. showing
     *  3. sliding down
     *
     * If the snackbar is either in state (1) or (2), then the content and color of the current
     * snackbar can be replaced. The problem is when the snackbar goes into state (3) as well as
     * the transition between (2) and (3) when it comes to thread sync. This is taken care of by
     * having a time difference in ms between the dissmiss event of previous snackbar and the one
     * trying to show next. In case this is too small, the current snackbar showing is forcebly
     * dismissed, and another one is showing.
     *
     * @param context   Context.
     * @param container Parent view where Snackbar will appear.
     * @param content   Message.
     */
    public static void showSnackbar(Context context, View container, String content, SnackbarDisplayType displayType) {
        Snackbar snackbar = null;
        if(lastShownSnackbar != null && lastShownSnackbar.isShown()) {
            snackbar = lastShownSnackbar;
            snackbar.setText(content);
        } else {
            snackbar = Snackbar.make(container, content, Snackbar.LENGTH_LONG);
            lastShownSnackbar = snackbar;
        }

        View snackbarView = snackbar.getView();
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, @DismissEvent int event) {
                //if the animation timedout - natural behaviour
                if(event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {
                    //check time difference
                    if((new Date()).getTime() - lastShownSnackbarTime < APPROXIMATE_SNACKBAR_TIME) {
                        // dismiss it
                        snackbar.dismiss();

                        // show it again
                        showSnackbar(context, container, content, displayType);
                    }
                }
            }
        });

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

        lastShownSnackbarTime = (new Date()).getTime();
        lastShownSnackbar = snackbar;
        snackbar.show();
    }

    public enum SnackbarDisplayType {
        NORMAL, FAILURE, FAILURE_BLUE, DROP
    }

}
