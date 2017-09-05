package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.NumberAbbreviator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CurrencyView extends LinearLayout {

    @BindView(R.id.hourglassTextView)
    TextView hourglassTextView;
    @BindView(R.id.gemTextView)
    TextView gemTextView;
    @BindView(R.id.goldTextView)
    TextView goldTextView;

    public CurrencyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public CurrencyView(Context context) {
        super(context);
        setupView();
    }

    private void setupView() {
        inflate(getContext(), R.layout.currency_view, this);

        ButterKnife.bind(this, this);
    }

    public void setGold(Double gold) {
        goldTextView.setText(NumberAbbreviator.abbreviate(getContext(), gold));
    }

    public void setGems(Integer gemCount) {
        gemTextView.setText(String.valueOf(gemCount));
        gemTextView.setVisibility(View.VISIBLE);
    }

    public void setHourglasses(Integer hourglassCount) {
        hourglassTextView.setText(String.valueOf(hourglassCount));
        if (hourglassCount > 0) {
            hourglassTextView.setVisibility(View.VISIBLE);
        } else {
            hourglassTextView.setVisibility(View.GONE);
        }
    }
}
