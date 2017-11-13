package com.habitrpg.android.habitica.ui.views.shops;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class PurchaseDialogContent extends LinearLayout {

    @BindView(R.id.imageView)
    SimpleDraweeView imageView;
    @BindView(R.id.titleTextView)
    TextView titleTextView;

    public PurchaseDialogContent(Context context) {
        super(context);
        setupView();
    }

    public PurchaseDialogContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    private void setupView() {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        inflate(getContext(), getViewId(), this);
        ButterKnife.bind(this, this);
    }

    protected abstract int getViewId();


    public void setItem(ShopItem item) {
        DataBindingUtils.INSTANCE.loadImage(imageView, item.getImageName());
        titleTextView.setText(item.getText());
    }
}
