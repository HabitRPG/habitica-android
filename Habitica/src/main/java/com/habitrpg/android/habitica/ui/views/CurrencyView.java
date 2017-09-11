package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.NumberAbbreviator;

public class CurrencyView extends android.support.v7.widget.AppCompatTextView {

    private boolean lightbackground = false;
    private String currency;
    private BitmapDrawable drawable;

    public CurrencyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CurrencyView(Context context, String currency, boolean lightbackground) {
        super(context);
        this.lightbackground = lightbackground;
        setCurrency(currency);
    }

    private void setIcon(Bitmap iconBitmap) {
        drawable = new BitmapDrawable(getResources(), iconBitmap);
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getContext().getResources().getDisplayMetrics());
        setCompoundDrawablePadding(padding);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        if ("gold".equals(currency)) {
            setIcon(HabiticaIconsHelper.imageOfGold());
            if (lightbackground) {
                setTextColor(ContextCompat.getColor(getContext(), R.color.yellow_50));
            } else {
                setTextColor(ContextCompat.getColor(getContext(), R.color.yellow_100));
            }
        } else if ("gems".equals(currency)) {
            setIcon(HabiticaIconsHelper.imageOfGem());
            setTextColor(ContextCompat.getColor(getContext(), R.color.green_50));
        } else if ("hourglasses".equals(currency)) {
            setIcon(HabiticaIconsHelper.imageOfHourglass());
            if (lightbackground) {
                setTextColor(ContextCompat.getColor(getContext(), R.color.brand_300));
            } else {
                setTextColor(ContextCompat.getColor(getContext(), R.color.brand_500));
            }
        }
        updateVisibility();
    }

    private void updateVisibility() {
        if ("hourglasses".equals(currency)) {
            setVisibility("0".equals(getText()) ? View.GONE : View.VISIBLE);
        }
    }

    public void setValue(Double value) {
        setText(NumberAbbreviator.abbreviate(getContext(), value));
        updateVisibility();
    }

    public void setLocked(boolean isLocked) {
        if (isLocked) {
            this.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_300));
            drawable.setAlpha(127);
        } else {
            drawable.setAlpha(255);
        }

        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }
}
