package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.habitrpg.android.habitica.R;

/**
 * Created by phillip on 08.09.17.
 */

public class CurrencyViews extends LinearLayout {
    private CurrencyView hourglassTextView;
    private CurrencyView goldTextView;
    private CurrencyView gemTextView;
    public boolean lightbackground;

    public CurrencyViews(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CurrencyViews,
                0, 0);

        lightbackground = attributes.getBoolean(R.styleable.CurrencyViews_hasLightBackground, true);

        setupViews();
    }

    public CurrencyViews(Context context) {
        super(context);
        setupViews();
    }

    private void setupViews() {
        Resources r = getContext().getResources();
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, r.getDisplayMetrics());

        hourglassTextView = new CurrencyView(getContext(), "hourglasses", lightbackground);
        this.addView(hourglassTextView);
        LinearLayout.LayoutParams hourglassParams = (LayoutParams) hourglassTextView.getLayoutParams();
        hourglassParams.setMargins(margin, 0, 0, 0);
        hourglassTextView.setLayoutParams(hourglassParams);
        goldTextView = new CurrencyView(getContext(), "gold", lightbackground);
        this.addView(goldTextView);
        LinearLayout.LayoutParams goldParams = (LayoutParams) goldTextView.getLayoutParams();
        goldParams.setMargins(margin, 0, 0, 0);
        goldTextView.setLayoutParams(goldParams);
        gemTextView = new CurrencyView(getContext(), "gems", lightbackground);
        this.addView(gemTextView);
        LinearLayout.LayoutParams gemParams = (LayoutParams) gemTextView.getLayoutParams();
        gemParams.setMargins(margin, 0, 0, 0);
        gemTextView.setLayoutParams(gemParams);
    }

    public void setGold(Double gold) {
        goldTextView.setValue(gold);
    }

    public void setGems(Integer gemCount) {
        gemTextView.setValue(Double.valueOf(gemCount));
    }

    public void setHourglasses(Integer hourglassCount) {
        hourglassTextView.setText(String.valueOf(hourglassCount));
    }
}
