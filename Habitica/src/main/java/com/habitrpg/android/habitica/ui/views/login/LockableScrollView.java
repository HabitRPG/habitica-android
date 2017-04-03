package com.habitrpg.android.habitica.ui.views.login;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockableScrollView extends ScrollView {

    private boolean scrollable = true;

    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollingEnabled(boolean enabled) {
        scrollable = enabled;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (scrollable) return super.onTouchEvent(ev);
                return scrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!scrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

}
