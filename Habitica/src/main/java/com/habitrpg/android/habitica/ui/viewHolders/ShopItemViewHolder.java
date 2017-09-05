package com.habitrpg.android.habitica.ui.viewHolders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShopItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.imageView)
    SimpleDraweeView imageView;
    @BindView(R.id.buyButton)
    View buyButton;
    @BindView(R.id.currency_icon_view)
    ImageView currencyIconView;
    @BindView(R.id.priceLabel)
    TextView priceLabel;

    @BindView(R.id.item_limited_icon)
    ImageView itemLimitedIcon;
    @BindView(R.id.item_locked_icon)
    ImageView itemLockedIcon;
    @BindView(R.id.item_count_icon)
    TextView itemCountView;

    public String shopIdentifier;
    private ShopItem item;

    private Context context;

    public ShopItemViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();

        ButterKnife.bind(this, itemView);

        itemView.setOnClickListener(this);
        itemView.setClickable(true);
    }

    public void bind(ShopItem item) {
        this.item = item;
        buyButton.setVisibility(View.VISIBLE);

        DataBindingUtils.loadImage(this.imageView, item.getImageName());

        if (item.getUnlockCondition() == null || !item.getLocked()) {
            priceLabel.setText(String.valueOf(item.getValue()));
            if (item.getCurrency().equals("gold")) {
                currencyIconView.setImageResource(R.drawable.currency_gold);
                priceLabel.setTextColor(ContextCompat.getColor(context, R.color.gold));
            } else if (item.getCurrency().equals("gems")) {
                currencyIconView.setImageResource(R.drawable.currency_gem);
                priceLabel.setTextColor(ContextCompat.getColor(context, R.color.good_10));
            } else if (item.getCurrency().equals("hourglasses")) {
                currencyIconView.setImageResource(R.drawable.currency_hourglass);
                priceLabel.setTextColor(ContextCompat.getColor(context, R.color.brand_300));
            } else {
                buyButton.setVisibility(View.GONE);
            }
        } else {
            priceLabel.setText(item.getUnlockCondition().readableUnlockConditionId());
        }

        if (item.isLimited()) {
            itemLimitedIcon.setVisibility(View.VISIBLE);
            itemCountView.setVisibility(View.GONE);
            itemLockedIcon.setVisibility(View.GONE);
        } else {
            itemLimitedIcon.setVisibility(View.GONE);
        }

        if (item.getLocked()) {
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.gray_300));
            currencyIconView.setAlpha(0.5f);
            itemLockedIcon.setVisibility(View.VISIBLE);
            itemCountView.setVisibility(View.GONE);
            itemLimitedIcon.setVisibility(View.GONE);
        } else {
            currencyIconView.setAlpha(1.0f);
            itemLockedIcon.setVisibility(View.GONE);
        }
    }

    public void setItemCount(int count) {
        if (count > 0) {
            itemCountView.setText(String.valueOf(count));
            itemLockedIcon.setVisibility(View.GONE);
            itemCountView.setVisibility(View.VISIBLE);
            itemLimitedIcon.setVisibility(View.GONE);
        } else {
            itemCountView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        PurchaseDialog dialog = new PurchaseDialog(context, HabiticaBaseApplication.getComponent(), item);
        dialog.shopIdentifier = shopIdentifier;
        dialog.show();
    }
}
