package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class FadingViewPager extends ViewPager {
    public FadingViewPager(Context context) {
        super(context);

        setTransformer();
    }

    public FadingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTransformer();
    }

    private void setTransformer() {
        this.setPageTransformer(true, (page, position) -> {
            page.setTranslationX(page.getWidth() * -position);

            if(position <= -1.0F || position >= 1.0F) {
                page.setAlpha(0.0F);
                page.setVisibility(View.INVISIBLE);
            } else if( position == 0.0F ) {
                page.setVisibility(View.VISIBLE);
                page.setAlpha(1.0F);
            } else {
                page.setVisibility(View.VISIBLE);
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                page.setAlpha(1.0F - Math.abs(position));
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return false;
    }
}
