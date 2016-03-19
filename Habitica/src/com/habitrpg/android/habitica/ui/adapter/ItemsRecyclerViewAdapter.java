package com.habitrpg.android.habitica.ui.adapter;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.DisplayFragmentEvent;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;
import com.magicmicky.habitrpgwrapper.lib.models.BaseItem;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ItemsRecyclerViewAdapter extends RecyclerView.Adapter<ItemsRecyclerViewAdapter.ItemViewHolder> {

    private List<BaseItem> itemsArrayList;

    public void setItemsArrayList(List<BaseItem> itemsArrayList) {
        this.itemsArrayList = itemsArrayList;
        this.notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bind(itemsArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.itemsArrayList == null ? 0 : this.itemsArrayList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.titleTextView)
        TextView textView;

        @Bind(R.id.imageView)
        ImageView imageView;

        BaseItem item;

        public ItemViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(BaseItem item) {
            this.item = item;
            this.textView.setText(this.item.getText());
            String imageUrl = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/shop_" + item.getKey() + ".png";

            Picasso.with(imageView.getContext())
                    .load(imageUrl)
                    .into(imageView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
