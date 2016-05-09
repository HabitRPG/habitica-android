package com.habitrpg.android.habitica.ui.adapter.inventory;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MountDetailRecyclerAdapter extends RecyclerView.Adapter<MountDetailRecyclerAdapter.MountViewHolder> {

    private List<Mount> itemList;
    private HashMap<String, Boolean> ownedMapping;
    public String itemType;

    public Context context;

    public void setItemList(List<Mount> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    public void setOwnedMapping(HashMap<String, Boolean> map) {
        this.ownedMapping = map;
        this.notifyDataSetChanged();
    }

    @Override
    public MountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.animal_overview_item, parent, false);

        return new MountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MountViewHolder holder, int position) {
        holder.bind(this.itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class MountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Mount animal;

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.imageView)
        ImageView imageView;

        @BindView(R.id.titleTextView)
        TextView titleView;

        @BindView(R.id.ownedTextView)
        TextView ownedTextView;

        Resources resources;

        public MountViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            resources = itemView.getResources();

            itemView.setOnClickListener(this);
        }

        public Boolean  isOwned() {
            if (ownedMapping != null && animal != null) {
                if (ownedMapping.containsKey(animal.getKey()) && ownedMapping.get(animal.getKey())) {
                    return true;
                }
            }
            return false;
        }

        public void bind(Mount item) {
            animal = item;
            titleView.setText(item.getColorText());
            ownedTextView.setVisibility(View.GONE);
            this.imageView.setAlpha(1.0f);
            if (this.isOwned()) {
                DataBindingUtils.loadImage(this.imageView, "Mount_Icon_" + itemType + "-" + item.getColor());
            } else {
                DataBindingUtils.loadImage(this.imageView, "PixelPaw");
                this.imageView.setAlpha(0.3f);
            }
        }

        @Override
        public void onClick(View v) {
            if (!this.isOwned()) {
                return;
            }
            BottomSheetMenu menu = new BottomSheetMenu(context);
            menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.use_animal)));
            menu.setSelectionRunnable(index -> {
                if (index == 0) {
                    EquipCommand event = new EquipCommand();
                    event.type = "mount";
                    event.key = animal.getKey();
                    EventBus.getDefault().post(event);
                }
            });
            menu.show();
        }
    }
}
