package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ValueBar extends FrameLayout {

    @BindView(R.id.ic_header)
    ImageView iconView;

    @BindView(R.id.valueLabel)
    TextView valueTextView;
    @BindView(R.id.descriptionLabel)
    TextView descriptionTextView;

    @BindView(R.id.bar_full)
    ViewGroup barBackground;
    @BindView(R.id.bar)
    View barView;
    @BindView(R.id.empty_bar_space)
    View barEmptySpace;


    public ValueBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.value_bar, this);
        ButterKnife.bind(this);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ValueBar,
                0, 0);
        setLightBackground(attributes.getBoolean(R.styleable.ValueBar_lightBackground, false));

        int color = attributes.getColor(R.styleable.ValueBar_barForegroundColor, 0);
        DataBindingUtils.INSTANCE.setRoundedBackground(barView, color);

        Drawable iconRes = attributes.getDrawable(R.styleable.ValueBar_barIconDrawable);
        if (iconRes != null) {
            setIcon(iconRes);
        }

        descriptionTextView.setText(attributes.getString(R.styleable.ValueBar_description));
    }

    public void setIcon(Drawable iconRes) {
        iconView.setImageDrawable(iconRes);
        iconView.setVisibility(View.VISIBLE);
    }

    public void setIcon(Bitmap bitmap) {
        iconView.setImageBitmap(bitmap);
        iconView.setVisibility(View.VISIBLE);
    }

    public void setBarWeight(double percent) {
        setLayoutWeight(barView, percent);
        setLayoutWeight(barEmptySpace, 1.0f - percent);
    }

    public void setValueText(String valueText) {
        valueTextView.setText(valueText);
    }

    public void setLightBackground(boolean lightBackground) {
        int textColor;
        if (lightBackground) {
            textColor = ContextCompat.getColor(getContext(), R.color.gray_10);
            barBackground.setBackgroundResource(R.drawable.layout_rounded_bg_light_gray);
        } else {
            textColor = ContextCompat.getColor(getContext(), R.color.brand_500);
            barBackground.setBackgroundResource(R.drawable.layout_rounded_bg_brand);
        }
        valueTextView.setTextColor(textColor);
        descriptionTextView.setTextColor(textColor);
    }

    public static void setLayoutWeight(View view, double weight) {
        view.clearAnimation();
        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (weight == 0.0f || weight == 1.0f) {
            layout.weight = (float) weight;
            view.setLayoutParams(layout);
        } else if (layout.weight != weight) {
            DataBindingUtils.LayoutWeightAnimation anim = new DataBindingUtils.LayoutWeightAnimation(view, (float) weight);
            anim.setDuration(1250);
            view.startAnimation(anim);
        }
    }

    public void set(double value, double valueMax) {
        double percent = Math.min(1, value / valueMax);

        this.setBarWeight(percent);
        this.setValueText((int) value + "/" + (int) valueMax);
    }
}
