package com.habitrpg.android.habitica.ui.adapter.inventory;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ItemItemBinding;
import com.habitrpg.android.habitica.events.ReloadContentEvent;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;
import com.magicmicky.habitrpgwrapper.lib.models.ShopCategory;
import com.magicmicky.habitrpgwrapper.lib.models.ShopItem;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShopRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> items;

    public void setShop(Shop shop) {
        items = new ArrayList<>();
        for (ShopCategory category : shop.categories) {
            if (category.items != null && category.items.size() > 0) {
                items.add(category);
                for (ShopItem item : category.items) {
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
                    .inflate(R.layout.customization_section_header, parent, false);

            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_shopitem, parent, false);

            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = this.items.get(position);
        if (obj.getClass().equals(ShopCategory.class)) {
            ((SectionViewHolder) holder).bind(((ShopCategory) obj).getText());
        } else {
            ((ItemViewHolder) holder).bind((ShopItem) items.get(position));

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.items.get(position).getClass().equals(ShopCategory.class)) {
            return 0;
        } else {
            return 1;
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

        ShopItem item;

        Context context;

        public ItemViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);
        }

        public void bind(ShopItem item) {
            this.item = item;
            titleView.setText(item.getText());
            descriptionView.setText(item.getNotes());

            DataBindingUtils.loadImage(this.imageView, item.getImageName());

            buyButton.setText(item.getValue().toString());
            switch (item.getCurrency()) {
                case "gold":
                    buyButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_header_gold, 0, 0, 0);
                    break;
                case "gems":
                    buyButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_header_gem, 0, 0, 0);
                    break;
                default:
                    buyButton.setVisibility(View.GONE);
            }
        }

        @NonNull
        private LinearLayout createContentViewForGearDialog() {
            String content = this.item.getNotes();

            // External ContentView
            LinearLayout contentViewLayout = new LinearLayout(context);
            contentViewLayout.setOrientation(LinearLayout.VERTICAL);

            // Gear Image
            ImageView gearImageView = new ImageView(context);
            LinearLayout.LayoutParams gearImageLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            gearImageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            gearImageLayoutParams.setMargins(0, 0, 0, 20);
            gearImageView.setMinimumWidth(200);
            gearImageView.setMinimumHeight(200);
            gearImageView.setLayoutParams(gearImageLayoutParams);
            DataBindingUtils.loadImage(gearImageView, item.getImageName());

            // Gear Description
            TextView contentTextView = new TextView(context, null);
            contentTextView.setPadding(16, 0, 16, 0);
            if (!content.isEmpty()) {
                contentTextView.setText(content);
            }

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
            TextView priceTextView = new TextView(context);
            priceTextView.setText(item.getValue().toString());
            priceTextView.setPadding(10, 0, 0, 0);

            ImageView currency = new ImageView(context);
            switch (item.getCurrency()) {
                case "gold":
                    currency.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_header_gold));
                    break;
                case "gems":
                    currency.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_header_gem));
                    break;
                default:
                    buyButton.setVisibility(View.GONE);
            }
            currency.setMinimumHeight(50);
            currency.setMinimumWidth(50);
            currency.setPadding(0, 0, 5, 0);

            goldPriceLayout.addView(currency);
            goldPriceLayout.addView(priceTextView);

            if (gearImageView.getDrawable() != null) {
                contentViewLayout.addView(gearImageView);
            }
            contentViewLayout.setGravity(Gravity.CENTER_VERTICAL);

            contentViewLayout.addView(goldPriceLayout);

            if (!content.isEmpty()) {
                contentViewLayout.addView(contentTextView);
            }

            return contentViewLayout;
        }

        private AlertDialog createDialog(LinearLayout contentViewForDialog) {
            return new AlertDialog.Builder(context)
                    .setPositiveButton(R.string.reward_dialog_buy, (dialog, which) -> {
                    })
                    .setTitle(this.item.getText())
                    .setView(contentViewForDialog)
                    .setNegativeButton(R.string.reward_dialog_dismiss, (dialog, which) -> {
                        dialog.dismiss();
                    }).create();
        }

        @Override
        public void onClick(View view) {
            AlertDialog dialog = createDialog(createContentViewForGearDialog());
            dialog.show();
        }
    }
}
