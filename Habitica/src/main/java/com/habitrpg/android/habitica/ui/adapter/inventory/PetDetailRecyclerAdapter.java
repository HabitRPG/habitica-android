package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.FeedCommand;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetDetailRecyclerAdapter extends RecyclerView.Adapter<PetDetailRecyclerAdapter.PetViewHolder> {

    public String itemType;
    public Context context;
    private List<Pet> itemList;
    private HashMap<String, Integer> ownedMapping;
    private HashMap<String, Boolean> ownedMountMapping;

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

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.imageView)
        SimpleDraweeView imageView;

        @BindView(R.id.titleTextView)
        TextView titleView;

        @BindView(R.id.trainedProgressBar)
        ProgressBar trainedProgressbar;

        Resources resources;

        public PetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            resources = itemView.getResources();

            itemView.setOnClickListener(this);
        }

        public int getOwnedStatus() {
            if (ownedMapping != null && animal != null) {
                if (ownedMapping.containsKey(animal.getKey())) {
                    return ownedMapping.get(animal.getKey());
                }
            }
            return 0;
        }

        public boolean isOwned() {
            return this.getOwnedStatus() > 0;
        }

        public Boolean isMountOwned() {
            if (animal.getAnimalGroup().equals("specialPets")) {
                return false;
            }
            if (ownedMountMapping != null && animal != null) {
                if (ownedMountMapping.get(animal.getKey()) != null) {
                    return true;
                }
            }
            return false;
        }

        public void bind(Pet item) {
            this.animal = item;
            this.titleView.setText(item.getColorText());
            this.trainedProgressbar.setVisibility(animal.getAnimalGroup().equals("specialPets") ? View.GONE : View.VISIBLE);
            this.imageView.setAlpha(1.0f);
            if (this.getOwnedStatus() > 0) {
                if (this.isMountOwned()) {
                    this.trainedProgressbar.setVisibility(View.GONE);
                } else {
                    this.trainedProgressbar.setProgress(ownedMapping.get(item.getKey()));
                }
                DataBindingUtils.loadImage(this.imageView, "Pet-" + itemType + "-" + item.getColor());
            } else {
                this.trainedProgressbar.setVisibility(View.GONE);
                if (this.getOwnedStatus() == 0) {
                    DataBindingUtils.loadImage(this.imageView, "PixelPaw");
                } else {
                    DataBindingUtils.loadImage(this.imageView, "Pet-" + itemType + "-" + item.getColor());
                }
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
            if (!animal.getAnimalGroup().equals("specialPets") && !this.isMountOwned()) {
                menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.feed)));
            }
            menu.setSelectionRunnable(index -> {
                if (index == 0) {
                    EquipCommand event = new EquipCommand();
                    event.type = "pet";
                    event.key = animal.getKey();
                    EventBus.getDefault().post(event);
                } else if (index == 1) {
                    FeedCommand event = new FeedCommand();
                    event.usingPet = animal;
                    EventBus.getDefault().post(event);
                }
            });
            menu.show();
        }
    }
}
