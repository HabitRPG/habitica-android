package com.habitrpg.android.habitica.ui.views.login;


import com.habitrpg.android.habitica.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

public class LoginBackgroundView extends RelativeLayout {

    private static float SIZE_FACTOR = 1.5f;
    private static int STAR_SIZE = 30;
    private static int STAR_OFFSET = 18;
    private final Random random;

    @BindView(R.id.left_cloud_view)
    ImageView leftCloudView;
    @BindView(R.id.right_cloud_view)
    ImageView rightCloudView;

    private List<StarView> starViews;
    private int width;
    private int height;
    private boolean didLayoutStars = false;

    public LoginBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.random = new Random();
        starViews = new ArrayList<>();
        generateStars();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.width = parentWidth;
        this.height = (int)(parentHeight*SIZE_FACTOR);
        this.setMeasuredDimension(width, height);
        this.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        updateStarLayoutParams();
    }

    public void generateStars() {
        generateStars(1, 10, 20);
    }

    private void generateStars(int largeCount, int mediumCount, int smallCount) {
        removeStarViews();
        for (int x = 0; x < largeCount; x++) {
            generateStar(2);
        }
        for (int x = 0; x < mediumCount; x++) {
            generateStar(1);
        }
        for (int x = 0; x < smallCount; x++) {
            generateStar(0);
        }
        requestLayout();
    }

    private void removeStarViews() {
        if (starViews.size() > 0) {
            starViews.forEach(this::removeView);
            starViews.clear();
        }
    }

    private void generateStar(int size) {
        StarView starView = new StarView(getContext()
        );
        starView.setStarSize(size);
        starViews.add(starView);
        if (width > 0 && height > 0) {
            this.addView(starView, getStarParams());
        } else {
            this.addView(starView);
        }
    }

    private void updateStarLayoutParams() {
        if (width <= 0 || height <= 0 || didLayoutStars || starViews.size() == 0) {
            return;
        }
        for (StarView view : starViews) {
            view.setLayoutParams(getStarParams());
        }
        didLayoutStars = true;
    }

    private RelativeLayout.LayoutParams getStarParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(STAR_SIZE, STAR_SIZE);
        params.leftMargin = random.nextInt(width);
        params.topMargin = random.nextInt(height);
        return params;
    }
}
