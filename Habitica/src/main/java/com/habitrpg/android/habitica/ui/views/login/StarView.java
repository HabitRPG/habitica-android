package com.habitrpg.android.habitica.ui.views.login;


import com.habitrpg.android.habitica.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class StarView extends AppCompatImageView {


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
                this.setImageResource(R.drawable.star_small);
                break;
            }
            case 1: {
                this.setImageResource(R.drawable.star_medium);
                break;
            }
            case 2: {
                this.setImageResource(R.drawable.star_large);
                break;
            }
        }
    }
}
