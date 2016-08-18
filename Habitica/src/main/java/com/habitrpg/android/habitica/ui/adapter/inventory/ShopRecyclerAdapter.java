package com.habitrpg.android.habitica.ui.adapter.inventory;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.ui.ItemDetailDialog;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;
import com.magicmicky.habitrpgwrapper.lib.models.ShopCategory;
import com.magicmicky.habitrpgwrapper.lib.models.ShopItem;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShopRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> items;
    private String shopIdentifier;

    public void setShop(Shop shop) {
        shopIdentifier = shop.identifier;
        items = new ArrayList<>();
        items.add(shop);
        for (ShopCategory category : shop.categories) {
            if (category.items != null && category.items.size() > 0) {
                items.add(category);
                for (ShopItem item : category.items) {
                    item.categoryIdentifier = category.getIdentifier();
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shop_header, parent, false);

            return new ShopHeaderViewHolder(view);
        } else if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.customization_section_header, parent, false);

            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_shopitem, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(view);
            viewHolder.shopIdentifier = shopIdentifier;
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = this.items.get(position);
        if (obj.getClass().equals(Shop.class)) {
            ShopHeaderViewHolder viewHolder = (ShopHeaderViewHolder) holder;
            Shop shop = (Shop)obj;
            DataBindingUtils.loadImage(viewHolder.imageView, shop.imageName);
            viewHolder.descriptionView.setText(Html.fromHtml(shop.getNotes()));
        } else if (obj.getClass().equals(ShopCategory.class)) {
            ((SectionViewHolder) holder).bind(((ShopCategory) obj).getText());
        } else {
            ((ItemViewHolder) holder).bind((ShopItem) items.get(position));

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.items.get(position).getClass().equals(Shop.class)) {
            return 0;
        } else if (this.items.get(position).getClass().equals(ShopCategory.class)) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.titleView)
        TextView titleView;
        @BindView(R.id.descriptionView)
        TextView descriptionView;
        @BindView(R.id.buyButton)
        Button buyButton;
        @BindView(R.id.unlockView)
        TextView unlockView;

        String shopIdentifier;
        ShopItem item;

        Context context;

        public ItemViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);

            buyButton.setOnClickListener(view -> {
                String currencyString = "";
                if (item.getCurrency().equals("gems")) {
                    if (item.getValue() == 1) {
                        currencyString = context.getString(R.string.gem);
                    } else {
                        currencyString = context.getString(R.string.gems);
                    }
                } else if (item.getCurrency().equals("gold")) {
                    if (item.getValue() == 1) {
                        currencyString = context.getString(R.string.gold_singular);
                    } else {
                        currencyString = context.getString(R.string.gold_plural);
                    }
                } else if (item.getCurrency().equals("hourglasses")) {
                    if (item.getValue() == 1) {
                        currencyString = context.getString(R.string.hourglass);
                    } else {
                        currencyString = context.getString(R.string.hourglasses);
                    }
                }
                new AlertDialog.Builder(context)
                        .setTitle(R.string.purchase_confirmation_title)
                        .setMessage(context.getString(R.string.confirm_purchase_text, item.getText(), item.getValue().toString(), currencyString))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> this.buyItem())
                        .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        private void buyItem() {
            BuyGemItemCommand command = new BuyGemItemCommand();
            command.shopIdentifier = shopIdentifier;
            command.item = item;
            EventBus.getDefault().post(command);
        }

        public void bind(ShopItem item) {
            this.item = item;
            buyButton.setVisibility(View.VISIBLE);
            titleView.setText(item.getText());
            descriptionView.setText(Html.fromHtml(item.getNotes()));

            DataBindingUtils.loadImage(this.imageView, item.getImageName());

            if (item.getUnlockCondition() == null) {
                buyButton.setText(item.getValue().toString());
                if (item.getCurrency().equals("gold")) {
                    buyButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_header_gold, 0, 0, 0);
                } else if (item.getCurrency().equals("gems")) {
                    buyButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_header_gem, 0, 0, 0);
                } else {
                    buyButton.setVisibility(View.GONE);
                }
                unlockView.setVisibility(View.GONE);
            } else {
                buyButton.setVisibility(View.GONE);
                unlockView.setVisibility(View.VISIBLE);
                unlockView.setText(item.unlockCondition.readableUnlockConditionId());
            }

            if (item.getLocked()) {
                buyButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            ItemDetailDialog dialog = new ItemDetailDialog(context);
            dialog.setTitle(item.getText());
            dialog.setDescription(Html.fromHtml(item.getNotes()));
            dialog.setImage(item.getImageName());
            if (item.getUnlockCondition() == null) {
                dialog.setCurrency(item.getCurrency());
                dialog.setValue(item.getValue());
                dialog.setBuyListener((clickedDialog, which) -> this.buyItem());
            }
            dialog.show();
        }
    }

    public static class ShopHeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageView)
        public ImageView imageView;

        @BindView(R.id.descriptionView)
        public TextView descriptionView;

        public ShopHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        
    }
}
