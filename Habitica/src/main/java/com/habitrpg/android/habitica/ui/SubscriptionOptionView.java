package com.habitrpg.android.habitica.ui;

import com.habitrpg.android.habitica.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SubscriptionOptionView extends FrameLayout {

    @BindView(R.id.priceLabel)
    TextView priceTextView;

    @BindView(R.id.descriptionTextView)
    TextView descriptionTextView;

    @BindView(R.id.subscriptionSelectedView)
    View subscriptionSelectedView;

    @BindView(R.id.subscriptionSelectedFrameView)
    View subscriptionSelectedFrameView;

    @BindView(R.id.gemCapTextView)
    TextView gemCapTextView;

    @BindView(R.id.hourglassTextView)
    TextView hourGlassTextView;

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

        gemCapTextView.setText(a.getText(R.styleable.SubscriptionOptionView_gemCapText));
        Integer hourGlassCount = a.getInteger(R.styleable.SubscriptionOptionView_hourGlassCount, 0);
        if (hourGlassCount != 0) {
            hourGlassTextView.setText(context.getString(R.string.subscription_hourglasses, hourGlassCount));
            hourGlassTextView.setVisibility(View.VISIBLE);
        } else {
            hourGlassTextView.setVisibility(View.GONE);
        }

    }

    public void setOnPurchaseClickListener(Button.OnClickListener listener) {
        this.setOnClickListener(listener);
    }

    public void setPriceText(String text) {
        this.priceTextView.setText(text);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setIsPurchased(boolean purchased) {
        int horizontalPadding = (int) getResources().getDimension(R.dimen.pill_horizontal_padding);
        int verticalPadding = (int) getResources().getDimension(R.dimen.pill_vertical_padding);
        if (purchased) {
            subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_selected);
            subscriptionSelectedFrameView.setBackgroundResource(R.color.brand_300);
            gemCapTextView.setBackgroundResource(R.drawable.pill_bg_green);
            gemCapTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            gemCapTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            hourGlassTextView.setBackgroundResource(R.drawable.pill_bg_green);
            hourGlassTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            hourGlassTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        } else {
            subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_unselected);
            subscriptionSelectedFrameView.setBackgroundResource(R.color.brand_700);
            gemCapTextView.setBackgroundResource(R.drawable.pill_bg);
            gemCapTextView.setTextColor(getContext().getResources().getColor(R.color.text_light));
            gemCapTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            hourGlassTextView.setBackgroundResource(R.drawable.pill_bg);
            hourGlassTextView.setTextColor(getContext().getResources().getColor(R.color.text_light));
            hourGlassTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        }
    }
}
