package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuSelectionRunnable;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PetDetailRecyclerAdapter extends RecyclerView.Adapter<PetDetailRecyclerAdapter.PetViewHolder> {

    private List<Pet> itemList;
    private HashMap<String, Integer> ownedMapping;
    private HashMap<String, Boolean> ownedMountMapping;
    public String itemType;

    public Context context;

    public void setItemList(List<Pet> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    public void setOwnedMapping(HashMap<String, Integer> map) {
        this.ownedMapping = map;
        this.notifyDataSetChanged();
    }

    public void setOwnedMountsMapping(HashMap<String, Boolean> map) {
        this.ownedMountMapping = map;
        this.notifyDataSetChanged();
    }

    @Override
    public PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pet_detail_item, parent, false);

        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PetViewHolder holder, int position) {
        holder.bind(this.itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class PetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Pet animal;

        @Bind(R.id.card_view)
        CardView cardView;

        @Bind(R.id.imageView)
        ImageView imageView;

        @Bind(R.id.titleTextView)
        TextView titleView;

        @Bind(R.id.trainedProgressBar)
        ProgressBar trainedProgressbar;

        Resources resources;

        public PetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            resources = itemView.getResources();

            itemView.setOnClickListener(this);
        }

        public Boolean  isOwned() {
            if (ownedMapping != null && animal != null) {
                if (ownedMapping.containsKey(animal.getKey()) && ownedMapping.get(animal.getKey()) != 0) {
                    return true;
                }
            }
            return false;
        }

        public Boolean isMountOwned() {
            if (animal.getAnimalGroup().equals("specialPets")) {
                return false;
            }
            if (ownedMountMapping != null && animal != null) {
                if (ownedMountMapping.containsKey(animal.getKey()) && ownedMountMapping.get(animal.getKey())) {
                    return true;
                }
            }
            return false;
        }

        public void bind(Pet item) {
            this.animal = item;
            this.titleView.setText(item.getColorText());
            this.trainedProgressbar.setVisibility(View.VISIBLE);
            this.imageView.setAlpha(1.0f);
            if (this.isOwned()) {
                if (this.isMountOwned()) {
                    this.trainedProgressbar.setVisibility(View.GONE);
                } else {
                    this.trainedProgressbar.setProgress(ownedMapping.get(item.getKey()));
                }
                DataBindingUtils.loadImage(this.imageView, "Pet-" + itemType + "-" + item.getColor());
            } else {
                this.trainedProgressbar.setVisibility(View.GONE);
                DataBindingUtils.loadImage(this.imageView, "PixelPaw");
                this.imageView.setAlpha(0.4f);
            }
        }

        @Override
        public void onClick(View v) {
            if (!this.isOwned()) {
                return;
            }
            BottomSheetMenu menu = new BottomSheetMenu(context);
            menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.use_animal)));
            if (!this.isMountOwned()) {
                menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.feed)));
            }
            menu.setSelectionRunnable(new BottomSheetMenuSelectionRunnable() {
                @Override
                public void selectedItemAt(Integer index) {
                    if (index == 0) {
                        EquipCommand event = new EquipCommand();
                        event.type = "pet";
                        event.key = animal.getKey();
                    } else if (index == 1) {
                        FeedCommand event = new FeedCommand();
                        event.usingPet = animal;
                        EventBus.getDefault().post(event);
                    }
                }
            });
            menu.show();
        }
    }
}
