package com.habitrpg.android.habitica.ui.helpers;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;

import net.pherth.android.emoji_library.EmojiTextView;

public class DataBindingUtils {

    public static void loadImage(SimpleDraweeView view, String imageName) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            view.setImageURI(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + imageName + ".png"));
        }
    }

    public static void setForegroundTintColor(TextView view, int color) {
        if (color > 0) {
            color = ContextCompat.getColor(view.getContext(), color);
        }
        view.setTextColor(color);
    }

    public static void setRoundedBackground(View view, int color) {
        Drawable drawable = ResourcesCompat.getDrawable(view.getResources(), R.drawable.layout_rounded_bg, null);
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        if (Build.VERSION.SDK_INT < 16) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }

    public static void setRoundedBackgroundInt(View view, int color) {
        if (color != 0) {
            setRoundedBackground(view, ContextCompat.getColor(view.getContext(), color));
        }
    }

    public static void bindEmojiconTextView(EmojiTextView textView, CharSequence value) {
        if (value != null) {
            textView.setText(MarkdownParser.parseMarkdown(value.toString()));
        }
    }

    public static class LayoutWeightAnimation extends Animation {
        float targetWeight;
        float initializeWeight;
        View view;

        LinearLayout.LayoutParams layoutParams;

        public LayoutWeightAnimation(View view, float targetWeight) {
            this.view = view;
            this.targetWeight = targetWeight;

            layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            initializeWeight = layoutParams.weight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            layoutParams.weight = initializeWeight + (targetWeight - initializeWeight) * interpolatedTime;

            view.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
