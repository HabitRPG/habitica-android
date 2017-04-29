package com.habitrpg.android.habitica.ui.helpers;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;

import net.pherth.android.emoji_library.EmojiTextView;

public class DataBindingUtils {

    @BindingAdapter("bind:imageName")
    public static void loadImage(SimpleDraweeView view, String imageName) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setImageURI(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + imageName + ".png"));
        }
    }

    @BindingAdapter("bind:questImageName")
    public static void loadQuestImage(SimpleDraweeView view, String imageName) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setImageURI(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + imageName + ".png"));
        }
    }

    @BindingAdapter("bind:cardColor")
    public static void setCardColor(CardView cardView, int color) {
        if (color > 0) {
            color = ContextCompat.getColor(cardView.getContext(), color);
        }
        cardView.setCardBackgroundColor(color);
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(CheckBox view, int color) {
        if (color > 0) {
            color = ContextCompat.getColor(view.getContext(), color);
        }
        ViewHelper.SetBackgroundTint(view, color);
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(Button view, int color) {
        if (color > 0) {
            color = ContextCompat.getColor(view.getContext(), color);
        }
        ViewHelper.SetBackgroundTint(view, color);
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(View view, int color) {
        if (color > 0) {
            color = ContextCompat.getColor(view.getContext(), color);
        }
        view.setBackgroundColor(color);
    }

    @BindingAdapter("app:foregroundColor")
    public static void setForegroundTintColor(TextView view, int color) {
        if (color > 0) {
            color = ContextCompat.getColor(view.getContext(), color);
        }
        view.setTextColor(color);
    }

    @BindingAdapter("android:layout_weight")
    public static void setLayoutWeight(View view, float weight) {
        view.clearAnimation();
        ValueBarBinding value_bar = DataBindingUtil.findBinding(view);
        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (weight == 0.0f || weight == 1.0f || value_bar.getPartyMembers()) {
            layout.weight = weight;
            view.setLayoutParams(layout);
        } else if (layout.weight != weight) {
            LayoutWeightAnimation anim = new LayoutWeightAnimation(view, weight);
            anim.setDuration(1250);
            view.startAnimation(anim);
        }
    }

    @BindingAdapter("app:rounded_background")
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

    @BindingAdapter("app:rounded_background_int")
    public static void setRoundedBackgroundInt(View view, int color) {
        if (color != 0) {
            setRoundedBackground(view, ContextCompat.getColor(view.getContext(), color));
        }
    }

    @BindingAdapter("parsemarkdown")
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
