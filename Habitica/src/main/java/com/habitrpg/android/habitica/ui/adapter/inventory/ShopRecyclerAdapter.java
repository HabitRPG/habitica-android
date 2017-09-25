package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopCategory;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.ShopItemViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ShopRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> items;
    private String shopIdentifier;
    private Map<String, Item> ownedItems = new HashMap<>();
    private String shopSpriteSuffix;

    public void setShop(Shop shop, String shopSpriteSuffix) {
        this.shopSpriteSuffix = shopSpriteSuffix;
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
            ShopItemViewHolder viewHolder = new ShopItemViewHolder(view);
            viewHolder.shopIdentifier = shopIdentifier;
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = this.items.get(position);
        if (obj.getClass().equals(Shop.class)) {
            ((ShopHeaderViewHolder) holder).bind((Shop) obj, shopSpriteSuffix);
        } else if (obj.getClass().equals(ShopCategory.class)) {
            ((SectionViewHolder) holder).bind(((ShopCategory) obj).getText());
        } else {
            ShopItem item = (ShopItem) items.get(position);
            ((ShopItemViewHolder) holder).bind(item);
            if (ownedItems.containsKey(item.getKey())) {
                ((ShopItemViewHolder) holder).setItemCount(ownedItems.get(item.getKey()).getOwned());
            }
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

    public void setOwnedItems(Map<String, Item> ownedItems) {
        this.ownedItems = ownedItems;
        this.notifyDataSetChanged();
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

        public void bind(Shop shop, String shopSpriteSuffix) {
            DataBindingUtils.loadImage(sceneView, shop.identifier+"_scene"+shopSpriteSuffix);

            backgroundView.setScaleType(ImageView.ScaleType.FIT_START);

            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + shop.identifier+"_background.png"+shopSpriteSuffix))
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
                        Observable.just(drawable)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(bitmapDrawable -> backgroundView.setBackground(bitmapDrawable), RxErrorHandler.handleEmptyError());
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
            namePlate.setText(shop.getNpcNameResource());
        }

    }
}
