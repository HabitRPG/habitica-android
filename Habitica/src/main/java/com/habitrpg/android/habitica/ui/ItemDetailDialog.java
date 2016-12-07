package com.habitrpg.android.habitica.ui;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemDetailDialog extends AlertDialog {

    private final SimpleDraweeView itemImageView;
    private final TextView contentTextView;
    private final TextView priceTextView;
    private final ImageView currencyImageView;

    public ItemDetailDialog(@NonNull Context context) {
        super(context);

        // External ContentView
        LinearLayout contentViewLayout = new LinearLayout(context);
        contentViewLayout.setOrientation(LinearLayout.VERTICAL);

        // Gear Image
        itemImageView = new SimpleDraweeView(context);
        LinearLayout.LayoutParams gearImageLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        gearImageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        gearImageLayoutParams.setMargins(0, 0, 0, 20);
        itemImageView.setMinimumWidth(200);
        itemImageView.setMinimumHeight(200);
        itemImageView.setLayoutParams(gearImageLayoutParams);
        itemImageView.setVisibility(View.GONE);

        // Gear Description
        contentTextView = new TextView(context, null);
        contentTextView.setPadding(16, 0, 16, 0);
        contentTextView.setVisibility(View.GONE);

        // GoldPrice View
        LinearLayout goldPriceLayout = new LinearLayout(context);
        goldPriceLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams goldPriceLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        goldPriceLayoutParams.setMargins(0, 0, 0, 16);
        goldPriceLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        goldPriceLayout.setOrientation(LinearLayout.HORIZONTAL);
        goldPriceLayout.setLayoutParams(goldPriceLayoutParams);
        goldPriceLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        // Price View
        priceTextView = new TextView(context);
        priceTextView.setPadding(10, 0, 0, 0);

        currencyImageView = new ImageView(context);
        currencyImageView.setMinimumHeight(50);
        currencyImageView.setMinimumWidth(50);
        currencyImageView.setPadding(0, 0, 5, 0);

        goldPriceLayout.addView(currencyImageView);
        goldPriceLayout.addView(priceTextView);

        contentViewLayout.setGravity(Gravity.CENTER_VERTICAL);

        contentViewLayout.addView(itemImageView);

        contentViewLayout.addView(goldPriceLayout);

        contentViewLayout.addView(contentTextView);

        setView(contentViewLayout);

        this.setButton(AlertDialog.BUTTON_NEGATIVE, context.getText(R.string.reward_dialog_dismiss), (clickedDialog, which) -> {
            clickedDialog.dismiss();
        });
    }

    public void setDescription(CharSequence description) {
        contentTextView.setText(description);
        contentTextView.setVisibility(View.VISIBLE);
    }

    public void setCurrency(String currency) {
        switch (currency) {
            case "gold":
                currencyImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_header_gold));
                break;
            case "gems":
                currencyImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_header_gem));
                break;
            default:
                currencyImageView.setImageDrawable(null);
                break;
        }
    }

    public void setValue(Double value) {
        priceTextView.setText(value.toString());
    }

    public void setValue(Integer value) {
        priceTextView.setText(value.toString());

    }

    public void setImage(String imageName) {
        itemImageView.setVisibility(View.VISIBLE);
        DataBindingUtils.loadImage(itemImageView, imageName);
    }

    public void setBuyListener(OnClickListener listener) {
        this.setButton(BUTTON_POSITIVE, getContext().getText(R.string.reward_dialog_buy), listener);
    }
}
