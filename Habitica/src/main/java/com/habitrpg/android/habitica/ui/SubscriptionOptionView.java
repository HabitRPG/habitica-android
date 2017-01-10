package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SubscriptionOptionView extends FrameLayout {

    @BindView(R.id.priceLabel)
    TextView priceTextView;

    @BindView(R.id.descriptionTextView)
    TextView descriptionTextView;

    @BindView(R.id.subscriptionSelectedView)
    View subscriptionSelectedView;

    private String sku;

    public SubscriptionOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.purchase_subscription_view, this);

        ButterKnife.bind(this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SubscriptionOptionView,
                0, 0);

        descriptionTextView.setText(context.getString(R.string.subscription_duration, a.getText(R.styleable.SubscriptionOptionView_recurringText)));

    }

    public void setOnPurchaseClickListener(Button.OnClickListener listener) {
        this.setOnClickListener(listener);
    }

    public void setPriceText(String text) {
        this.priceTextView.setText(text);
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    public void setIsPurchased(boolean purchased) {
        if (purchased) {
            subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_selected);
        } else {
            subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_unselected);
        }
    }
}
