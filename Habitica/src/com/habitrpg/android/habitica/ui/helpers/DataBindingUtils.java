package com.habitrpg.android.habitica.ui.helpers;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.data5tream.emojilib.EmojiTextView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.squareup.picasso.Picasso;

/**
 * Created by Negue on 05.09.2015.
 */
public class DataBindingUtils {

    @BindingAdapter("bind:imageName")
    public static void loadImage(ImageView view, String imageName) {
        if (view.getVisibility() == View.VISIBLE) {
            Picasso.with(view.getContext())
                    .load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + imageName + ".png")
                    .into(view);
        }
    }

    @BindingAdapter("bind:questImageName")
    public static void loadQuestImage(ImageView view, String imageName) {
        if (view.getVisibility() == View.VISIBLE) {
            Picasso.with(view.getContext())
                    .load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + imageName + ".png")
                    .resizeDimen(R.dimen.quest_image_width, R.dimen.quest_image_height)
                    .into(view);
        }
    }

    @BindingAdapter("bind:cardColor")
    public static void setCardColor(CardView cardView, int color) {
        if (color > 0) {
            color = cardView.getResources().getColor(color);
        }
        cardView.setCardBackgroundColor(color);
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(CheckBox view, int color) {
        if (color > 0) {
            color = view.getResources().getColor(color);
        }
        ViewHelper.SetBackgroundTint(view, color);
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(Button view, int color) {
        if (color > 0) {
            color = view.getResources().getColor(color);
        }
        ViewHelper.SetBackgroundTint(view, color);
    }

    @BindingAdapter("app:backgroundColor")
    public static void setBackgroundTintColor(View view, int color) {
        if (color > 0) {
            color = view.getResources().getColor(color);
        }
        view.setBackgroundColor(color);
    }

    @BindingAdapter("app:foregroundColor")
    public static void setForegroundTintColor(TextView view, int color) {
        if (color > 0) {
            color = view.getResources().getColor(color);
        }
        view.setTextColor(color);
    }

    @BindingAdapter("android:layout_weight")
    public static void setLayoutWeight(View view, float weight) {
        ValueBarBinding value_bar = DataBindingUtil.findBinding(view);
        if (weight == 0.0f || weight == 1.0f || value_bar.getPartyMembers()) {
            LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) view.getLayoutParams();
            layout.weight = weight;
            view.setLayoutParams(layout);
        } else {
            LayoutWeightAnimation anim = new LayoutWeightAnimation(view, weight);
            anim.setDuration(1250);
            view.startAnimation(anim);
        }
    }

    @BindingAdapter("app:rounded_background")
    public static void setRoundedBackground(View view, int color) {
        Drawable drawable = view.getResources().getDrawable(R.drawable.layout_rounded_bg);
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        if (Build.VERSION.SDK_INT < 16) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }

    @BindingAdapter("app:rounded_background_int")
    public static void setRoundedBackgroundInt(View view, int color) {
        if (color != 0) {
            setRoundedBackground(view, view.getResources().getColor(color));
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
