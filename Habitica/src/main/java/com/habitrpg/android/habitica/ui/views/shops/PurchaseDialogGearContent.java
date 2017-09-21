package com.habitrpg.android.habitica.ui.views.shops;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.Gear;

import butterknife.BindView;

class PurchaseDialogGearContent extends PurchaseDialogContent {


    @BindView(R.id.notesTextView)
    TextView notesTextView;
    @BindView(R.id.str_label)
    TextView strLabel;
    @BindView(R.id.str_value)
    TextView strValueTextView;
    @BindView(R.id.per_label)
    TextView perLabel;
    @BindView(R.id.per_value)
    TextView perValueTextView;
    @BindView(R.id.con_label)
    TextView conLabel;
    @BindView(R.id.con_value)
    TextView conValueTextView;
    @BindView(R.id.int_label)
    TextView intLabel;
    @BindView(R.id.int_value)
    TextView intValueTextView;

    public PurchaseDialogGearContent(Context context) {
        super(context);
    }

    public PurchaseDialogGearContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getViewId() {
        return R.layout.dialog_purchase_content_gear;
    }

    @Override
    public void setItem(ShopItem item) {
        super.setItem(item);
        notesTextView.setText(item.getNotes());
    }

    public void setEquipment(Equipment equipment) {
        configureFieldsForValue(strLabel, strValueTextView, equipment.str);
        configureFieldsForValue(perLabel, perValueTextView, equipment.per);
        configureFieldsForValue(conLabel, conValueTextView, equipment.con);
        configureFieldsForValue(intLabel, intValueTextView, equipment._int);
    }

    private void configureFieldsForValue(TextView labelView, TextView valueTextView, int value) {
        valueTextView.setText("+"+value);
        if (value == 0) {
            labelView.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_400));
            valueTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_400));
        }
    }
}
