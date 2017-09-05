package com.habitrpg.android.habitica.ui.views.setup;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvatarCategoryView extends LinearLayout {

    private final Drawable icon;
    @BindView(R.id.text_view)
    TextView textView;

    public AvatarCategoryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.avatar_category, this);

        ButterKnife.bind(this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AvatarCategoryView,
                0, 0);

        textView.setText(a.getText(R.styleable.AvatarCategoryView_categoryTitle));

        icon = a.getDrawable(R.styleable.AvatarCategoryView_iconDrawable);
        if (icon != null) {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        }
        setActive(false);
    }

    public void setActive(boolean active) {
        int color;
        if (active) {
            color = ContextCompat.getColor(getContext(), R.color.white);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.white_50_alpha);
        }
        textView.setTextColor(color);
        if (icon != null) {
            icon.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        }
    }
}
