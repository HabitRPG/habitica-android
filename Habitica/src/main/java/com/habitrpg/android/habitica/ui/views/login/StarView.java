package com.habitrpg.android.habitica.ui.views.login;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import java.util.List;

public class StarView extends AppCompatImageView {


    private List<Integer> blinkDurations;
    private int blinkIndex = 0;

    public StarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StarView(Context context) {
        super(context);
        this.setScaleType(ScaleType.CENTER);
    }

    public void setStarSize(int size) {
        switch (size) {
            case 0: {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarSmall());
                break;
            }
            case 1: {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarMedium());
                break;
            }
            case 2: {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarLarge());
                break;
            }
        }
    }

    public void setBlinkDurations(List<Integer> blinkDurations) {
        this.blinkDurations = blinkDurations;
        runBlink();
    }

    private void runBlink() {
        if (blinkIndex >= blinkDurations.size()) {
            blinkIndex = 0;
        }
        ValueAnimator animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0);
        animator.setDuration(1000);
        animator.setStartDelay(blinkDurations.get(blinkIndex));
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                blinkIndex++;
                runBlink();
            }
        });
        try {
            animator.start();
        } catch (NullPointerException ignored) {
        }
    }
}
