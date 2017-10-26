package com.habitrpg.android.habitica.ui.views.shops;

import android.content.Context;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.shops.ShopItem;

import butterknife.BindView;

/**
 * Created by phillip on 25.07.17.
 */

class PurchaseDialogGemsContent extends PurchaseDialogContent {

    @BindView(R.id.notesTextView)
    TextView notesTextView;

    public PurchaseDialogGemsContent(Context context) {
        super(context);
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
