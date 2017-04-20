package com.habitrpg.android.habitica.ui.helpers;

// https://gist.github.com/lodlock/e3cd12130bad70a098db

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.clans.fab.FloatingActionMenu;
import com.habitrpg.android.habitica.R;

import java.util.List;

public class FloatingActionMenuBehavior extends CoordinatorLayout.Behavior {

    private final int FAB_ANIMATION_DURATION = 500;

    private float mTranslationY;

    private Context context;
    private boolean isAnimating;
    private boolean isOffScreen;
    @Nullable
    private FloatingActionMenu fab;

    public FloatingActionMenuBehavior(Context context, AttributeSet attrs) {
        super();
        this.context = context;
        isAnimating = false;
        isOffScreen = false;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (child instanceof FloatingActionMenu && dependency instanceof Snackbar.SnackbarLayout) {
            this.updateTranslation(parent, child, dependency);
        }

        return false;
    }

    private void updateTranslation(CoordinatorLayout parent, View child, View dependency) {
        float translationY = this.getTranslationY(parent, child);
        if (translationY != this.mTranslationY) {
            ViewCompat.animate(child)
                    .cancel();
            if (Math.abs(translationY - this.mTranslationY) == (float) dependency.getHeight()) {
                ViewCompat.animate(child)
                        .translationY(translationY)
                        .setListener(null);
            } else {
                ViewCompat.setTranslationY(child, translationY);
            }

            this.mTranslationY = translationY;
        }

    }

    private float getTranslationY(CoordinatorLayout parent, View child) {
        float minOffset = 0.0F;
        List dependencies = parent.getDependencies(child);
        int i = 0;

        for (int z = dependencies.size(); i < z; ++i) {
            View view = (View) dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float) view.getHeight());
            }
        }

        return minOffset;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        ViewGroup fabContainer = (ViewGroup) child.findViewById(R.id.floating_menu_wrapper);
        if (fabContainer.getChildCount() > 0) {
            if (fabContainer.getChildAt(0).getClass().equals(FloatingActionMenu.class)) {
                fab = (FloatingActionMenu) fabContainer.getChildAt(0);
            }
        }
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, final View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (fab == null) {
            return;
        }

        /*
        Logic:
            - If we're scrolling downwards or we're at the bottom of the screen
            AND if we're not animating and not on screen > HIDE
         */
        if ((dyConsumed > 20 || (dyConsumed == 0 && dyUnconsumed > 0)) && !isAnimating && !isOffScreen) {
            isAnimating = true;
            slideFabOffScreen(fab);
            resetAnimatingStatusWithDelay(fab);
            isOffScreen = true;

        /*
         Logic:
            - If we're not on screen
            AND we're scrolling upwards and not animating OR we're at the top of the screen > SHOW
         */
        } else if (isOffScreen && ((dyConsumed < -10 && !isAnimating) || dyUnconsumed < 0)) {
            isAnimating = true;
            slideFabOnScreen(fab);
            resetAnimatingStatusWithDelay(fab);
            isOffScreen = false;
        }
    }

    private void resetAnimatingStatusWithDelay(final FloatingActionMenu child) {
        child.postDelayed(() -> {
            isAnimating = false;
            if (isOffScreen && fab != null) {
                child.hideMenu(false);
            }
        }, FAB_ANIMATION_DURATION);
    }

    private void slideFabOffScreen(FloatingActionMenu view) {
        Animation slideOff = AnimationUtils.loadAnimation(context, R.anim.fab_slide_out);
        slideOff.setDuration(FAB_ANIMATION_DURATION);
        slideOff.setFillAfter(true);
        view.startAnimation(slideOff);
    }

    private void slideFabOnScreen(FloatingActionMenu view) {
        Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.fab_slide_in);
        slideIn.setDuration(FAB_ANIMATION_DURATION);
        slideIn.setFillAfter(true);
        view.startAnimation(slideIn);
        view.showMenu(false);
    }
}