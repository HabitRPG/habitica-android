package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.BuyGemItemCommand;
import com.habitrpg.android.habitica.helpers.TiledBitmapPostProcessor;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopCategory;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.ui.ItemDetailDialog;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder;

import org.greenrobot.eventbus.EventBus;

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
                    .inflate(R.layout.shop_section_header, parent, false);

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
            ((ShopHeaderViewHolder) holder).bind((Shop) obj);
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

    public void updateGoldGemCount(int numberLeft) {
        int itemPos = 0;
        for (Object obj : items) {
            if (obj.getClass().equals(ShopItem.class)) {
                ShopItem item = (ShopItem) obj;
                if (item.key.equals(ShopItem.GEM_FOR_GOLD)) {
                    item.limitedNumberLeft = numberLeft;
                    break;
                }
            }
            itemPos++;
        }
        notifyItemChanged(itemPos);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        String shopIdentifier;
        ShopItem item;

        Context context;

        public ItemViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);
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

            DataBindingUtils.loadImage(this.imageView, item.getImageName());

            if (item.getUnlockCondition() == null) {
                priceLabel.setText(item.getValue().toString());
                if (item.getCurrency().equals("gold")) {
                    currencyIconView.setImageResource(R.drawable.currency_gold);
                    priceLabel.setTextColor(ContextCompat.getColor(context, R.color.gold));
                } else if (item.getCurrency().equals("gems")) {
                    currencyIconView.setImageResource(R.drawable.currency_gem);
                    priceLabel.setTextColor(ContextCompat.getColor(context, R.color.good_10));
                } else {
                    buyButton.setVisibility(View.GONE);
                }
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

    static class ShopHeaderViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        @BindView(R.id.sceneView)
        public SimpleDraweeView sceneView;
        @BindView(R.id.backgroundView)
        public ImageView backgroundView;

        @BindView(R.id.name_plate)
        public TextView namePlate;

        @BindView(R.id.descriptionView)
        public TextView descriptionView;


        ShopHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
            descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        public void bind(Shop shop) {
            DataBindingUtils.loadImage(sceneView, shop.identifier+"_scene");

            backgroundView.setScaleType(ImageView.ScaleType.FIT_START);

            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + shop.identifier+"_background.png"))
                    .build();

            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            final DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

            dataSource.subscribe(new BaseBitmapDataSubscriber() {

                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    if (dataSource.isFinished() && bitmap != null){
                        float aspectRatio = bitmap.getWidth() /
                                (float) bitmap.getHeight();
                        int height = (int) context.getResources().getDimension(R.dimen.shop_height);
                        int width = Math.round(height * aspectRatio);
                        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, width, height, false));
                        drawable.setTileModeX(Shader.TileMode.REPEAT);
                        backgroundView.setBackground(drawable);
                        dataSource.close();
                    }
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    if (dataSource != null) {
                        dataSource.close();
                    }
                }
            }, CallerThreadExecutor.getInstance());

            descriptionView.setText(Html.fromHtml(shop.getNotes()));
            switch (shop.getIdentifier()) {
                case "market":
                    namePlate.setText(R.string.market_owner);
                    break;
                case "questShop":
                    namePlate.setText(R.string.questShop_owner);
                    break;
                case "seasonalShop":
                    namePlate.setText(R.string.seasonalShop_owner);
                    break;
                case "timetravelers":
                    namePlate.setText(R.string.timetravelers_owner);
            }
        }

    }
}
