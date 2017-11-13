package com.habitrpg.android.habitica.ui.views;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils;

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
        if (NavbarUtils.hasSoftKeys(parent.getContext())) {
            int[] parentLocation = new int[2];
            parent.getLocationInWindow(parentLocation);
            if (NavbarUtils.isBehindNavbar(parentLocation, parent.getContext())) {
                content.setPadding(0, 0, 0, NavbarUtils.getNavbarHeight(parent.getContext()));
            }
        }
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

    public HabiticaSnackbar setRightDiff(Drawable icon, int textColor, String text) {
        if (icon == null) {
            return this;
        }
        View rightView = getView().findViewById(R.id.rightView);
        rightView.setVisibility(View.VISIBLE);
        ImageView rightIconView = getView().findViewById(R.id.rightIconView);
        rightIconView.setImageDrawable(icon);
        TextView rightTextView = getView().findViewById(R.id.rightTextView);
        rightTextView.setTextColor(textColor);
        rightTextView.setText(text);
        return this;
    }

    public HabiticaSnackbar setLeftIcon(Drawable image) {
        if (image == null) {
            return this;
        }
        ImageView imageView = getView().findViewById(R.id.leftImageView);
        imageView.setImageDrawable(image);
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
            LinearLayout snackbarView = (LinearLayout) getView().findViewById(R.id.content_container);
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
            content.setScaleY(0f);
            ViewCompat.animate(content).scaleY(1f).setDuration(duration).setStartDelay(delay);
            ViewCompat.animate(content).alpha(1f).setDuration(duration).setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            content.setScaleY(1);
            ViewCompat.animate(content).scaleY(0f).setDuration(duration).setStartDelay(delay);
            ViewCompat.animate(content).alpha(0f).setDuration(duration).setStartDelay(delay);
        }
    }


    public static void showSnackbar(ViewGroup container, CharSequence content, SnackbarDisplayType displayType) {
        showSnackbar(container, null, null, content, null, null, 0, null, displayType);
    }

    public static void showSnackbar(ViewGroup container, Drawable leftImage, CharSequence title, CharSequence content, SnackbarDisplayType displayType) {
        showSnackbar(container, leftImage, title, content, null, null, 0, null, displayType);
    }


    public static void showSnackbar(ViewGroup container, CharSequence title, CharSequence content, Drawable rightIcon, int rightTextColor, String rightText, SnackbarDisplayType displayType) {
        showSnackbar(container, null, title, content, null, rightIcon, rightTextColor, rightText, displayType);
    }

    public static void showSnackbar(ViewGroup container, CharSequence title, CharSequence content, View specialView, SnackbarDisplayType displayType) {
        showSnackbar(container, null, title, content, specialView, null, 0, null, displayType);
    }

    public static void showSnackbar(ViewGroup container, Drawable leftImage, CharSequence title, CharSequence content, View specialView, Drawable rightIcon, int rightTextColor, String rightText, SnackbarDisplayType displayType) {
        HabiticaSnackbar snackbar = HabiticaSnackbar.make(container, Snackbar.LENGTH_LONG)
                .setTitle(title)
                .setText(content)
                .setSpecialView(specialView)
                .setLeftIcon(leftImage)
                .setRightDiff(rightIcon, rightTextColor, rightText);

        switch (displayType) {
            case FAILURE:
                snackbar.setBackgroundResource(R.drawable.snackbar_background_red);
                break;
            case FAILURE_BLUE:
            case BLUE:
                snackbar.setBackgroundResource(R.drawable.snackbar_background_blue);
                break;
            case DROP:
            case NORMAL:
                snackbar.setBackgroundResource(R.drawable.snackbar_background_gray);
                break;
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
