package com.habitrpg.android.habitica.ui.views;


import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import com.habitrpg.android.habitica.R;

// http://stackoverflow.com/a/6700718/1315039
public class Typewriter extends android.support.v7.widget.AppCompatTextView {

    private SpannableStringBuilder stringBuilder;
    private Object visibleSpan;
    private Object hiddenSpan;
    private int index;
    private long delay = 30;


    public Typewriter(Context context) {
        super(context);
        setupTextColors(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupTextColors(context);
    }

    private void setupTextColors(Context context) {
        visibleSpan = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.textColorLight));
        hiddenSpan = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.transparent));
    }

    private Handler handler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            stringBuilder.setSpan(visibleSpan, 0, index++, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            setText(stringBuilder);
            if(index <= stringBuilder.length()) {
                handler.postDelayed(characterAdder, delay);
            }
        }
    };

    public void animateText(CharSequence text) {
        stringBuilder = new SpannableStringBuilder(text);
        stringBuilder.setSpan(hiddenSpan, 0, stringBuilder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        index = 0;

        setText(stringBuilder);
        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay);
    }

    public void setCharacterDelay(long millis) {
        delay = millis;
    }

    public boolean isAnimating() {
        return index < stringBuilder.length();
    }

    public void stopTextAnimation() {
        index = stringBuilder.length();
    }
}
