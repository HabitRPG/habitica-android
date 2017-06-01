package com.habitrpg.android.habitica.ui;

import com.habitrpg.android.habitica.R;

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

import butterknife.BindView;
import butterknife.ButterKnife;


public class GemPurchaseOptionsView extends FrameLayout {

    @BindView(R.id.seedsImageButton)
    public
    ImageButton seedsImageButton;
    @BindView(R.id.gem_image)
    ImageView gemImageView;
    @BindView(R.id.gem_amount)
    TextView gemAmountTextView;
    @BindView(R.id.purchase_button)
    Button purchaseButton;
    private String sku;

    public GemPurchaseOptionsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.purchase_gem_view, this);

        ButterKnife.bind(this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GemPurchaseOptionsView,
                0, 0);

        gemAmountTextView.setText(a.getText(R.styleable.GemPurchaseOptionsView_gemAmount));

        Drawable iconRes = a.getDrawable(R.styleable.GemPurchaseOptionsView_gemDrawable);
        if (iconRes != null) {
            gemImageView.setImageDrawable(iconRes);
        }

        if (a.getBoolean(R.styleable.GemPurchaseOptionsView_showSeedsPromo, false)) {
            seedsImageButton.setVisibility(View.VISIBLE);
        }
    }

    public void setOnPurchaseClickListener(Button.OnClickListener listener) {
        purchaseButton.setOnClickListener(listener);
    }

    public void setPurchaseButtonText(String price) {
        purchaseButton.setText(price);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
