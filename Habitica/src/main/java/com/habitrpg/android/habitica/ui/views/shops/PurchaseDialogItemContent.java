package com.habitrpg.android.habitica.ui.views.shops;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.shops.ShopItem;

import butterknife.BindView;

public class PurchaseDialogItemContent extends PurchaseDialogContent {

    @BindView(R.id.notesTextView)
    TextView notesTextView;

    public PurchaseDialogItemContent(Context context) {
        super(context);
    }

    public PurchaseDialogItemContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getViewId() {
        return R.layout.dialog_purchase_content_item;
    }

    @Override
    public void setItem(ShopItem item) {
        super.setItem(item);

        notesTextView.setText(item.getNotes());
    }
}
