package com.habitrpg.android.habitica.ui.views.shops;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.habitrpg.android.habitica.R;

public class PurchaseDialogBaseContent extends PurchaseDialogContent {

    public PurchaseDialogBaseContent(Context context) {
        super(context);
    }

    public PurchaseDialogBaseContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getViewId() {
        return R.layout.dialog_purchase_content_item;
    }

}
