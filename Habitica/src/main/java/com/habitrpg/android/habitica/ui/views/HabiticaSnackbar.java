package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import javax.annotation.Resource;

public class HabiticaSnackbar extends BaseTransientBottomBar<HabiticaSnackbar> {

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent The parent for this transient bottom bar.
     * @param content The content view for this transient bottom bar.
     * @param callback The content view callback for this transient bottom bar.
     */
    private HabiticaSnackbar(ViewGroup parent, View content, ContentViewCallback callback) {
        super(parent, content, callback);
    }

    private static HabiticaSnackbar make(@NonNull ViewGroup parent, int duration) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View content = inflater.inflate(R.layout.snackbar_view, parent, false);
        final ContentViewCallback viewCallback = new ContentViewCallback(content);
        final HabiticaSnackbar customSnackbar = new HabiticaSnackbar(parent, content, viewCallback);
        customSnackbar.setDuration(duration);
        return customSnackbar;
    }

    public HabiticaSnackbar setTitle(CharSequence title) {
        TextView textView = (TextView) getView().findViewById(R.id.snackbar_title);
        textView.setText(title);
        textView.setVisibility(title != null ? View.VISIBLE : View.GONE);
        return this;
    }

    public HabiticaSnackbar setText(CharSequence text) {
        TextView textView = (TextView) getView().findViewById(R.id.snackbar_text);
        textView.setText(text);
        textView.setVisibility(text != null ? View.VISIBLE : View.GONE);
        return this;
    }

    public HabiticaSnackbar setBackgroundColor(@ColorInt int color) {
        getView().setBackgroundColor(color);
        return this;
    }

    public HabiticaSnackbar setBackgroundResource(int resourceId) {
        View snackbarView = getView().findViewById(R.id.snackbar_view);
        snackbarView.setBackgroundResource(resourceId);
        getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        return this;
    }

    private HabiticaSnackbar setSpecialView(View specialView) {
        if (specialView != null) {
            LinearLayout snackbarView = (LinearLayout) getView().findViewById(R.id.snackbar_view);
            snackbarView.addView(specialView);
        }
        return this;
    }

    private static class ContentViewCallback implements BaseTransientBottomBar.ContentViewCallback {

        private View content;

        public ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            ViewCompat.setScaleY(content, 0f);
            ViewCompat.animate(content).scaleY(1f).setDuration(duration).setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            ViewCompat.setScaleY(content, 1f);
            ViewCompat.animate(content).scaleY(0f).setDuration(duration).setStartDelay(delay);
        }
    }

    /**
     * Shows snackbar in given container.
     *
     * @param context   Context.
     * @param container Parent view where Snackbar will appear.
     * @param content   message.
     */
    public static void showSnackbar(Context context, ViewGroup container, CharSequence content, SnackbarDisplayType displayType) {
        showSnackbar(context, container, null, content, null, displayType);
    }

    /**
     * Shows snackbar in given container.
     *
     * @param context   Context.
     * @param container Parent view where Snackbar will appear.
     * @param content   message.
     */
    public static void showSnackbar(Context context, ViewGroup container, CharSequence title, CharSequence content, View specialView, SnackbarDisplayType displayType) {
        HabiticaSnackbar snackbar = HabiticaSnackbar.make(container, Snackbar.LENGTH_LONG)
                .setTitle(title)
                .setText(content)
                .setSpecialView(specialView);

        switch (displayType) {
            case FAILURE:
                snackbar.setBackgroundResource(R.drawable.snackbar_background_red);
                break;
            case FAILURE_BLUE:
            case BLUE:
            case DROP:
                snackbar.setBackgroundResource(R.drawable.snackbar_background_blue);
                break;
            case NORMAL:
            case SUCCESS:
                snackbar.setBackgroundResource(R.drawable.snackbar_background_green);
                break;
        }

        snackbar.show();
    }

    public enum SnackbarDisplayType {
        NORMAL, FAILURE, FAILURE_BLUE, DROP, SUCCESS, BLUE
    }

}
