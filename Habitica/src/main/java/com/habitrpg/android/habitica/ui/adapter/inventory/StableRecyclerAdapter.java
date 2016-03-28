package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.inventory.MountDetailRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.PetDetailRecyclerFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Animal;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StableRecyclerAdapter extends RecyclerView.Adapter<StableRecyclerAdapter.StableViewHolder> {

    private List<Animal> itemList;
    private HashMap<String, Integer> ownedMapping;
    public String itemType;

    public MainActivity activity;

    public <T extends Animal> void setItemList(List<Animal> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    public void setOwnedMapping(HashMap<String, Integer> map) {
        this.ownedMapping = map;
        this.notifyDataSetChanged();
    }

    @Override
    public StableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.animal_overview_item, parent, false);

        return new StableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StableViewHolder holder, int position) {
        holder.bind(this.itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class StableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Animal animal;

        @Bind(R.id.card_view)
        CardView cardView;

        @Bind(R.id.imageView)
        ImageView imageView;

        @Bind(R.id.titleTextView)
        TextView titleView;

        @Bind(R.id.ownedTextView)
        TextView ownedTextView;

        Resources resources;

        public StableViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            resources = itemView.getResources();

            itemView.setOnClickListener(this);
        }

        public void bind(Animal item) {
            this.animal = item;
            titleView.setText(item.getAnimal());
            ownedTextView.setVisibility(View.VISIBLE);
            this.imageView.setAlpha(1.0f);
            if (ownedMapping != null) {
                if (ownedMapping.containsKey(item.getAnimal()) && ownedMapping.get(item.getAnimal()) > 0) {
                    this.ownedTextView.setText(ownedMapping.get(item.getAnimal()).toString());
                    if (itemType.equals("pets")) {
                        DataBindingUtils.loadImage(this.imageView, "Pet-" + item.getAnimal() + "-Base");
                    } else {
                        DataBindingUtils.loadImage(this.imageView, "Mount_Icon_" + item.getAnimal() + "-Base");
                    }
                } else {
                    ownedTextView.setVisibility(View.GONE);
                    ownedTextView.setText(null);
                    DataBindingUtils.loadImage(this.imageView, "PixelPaw");
                    this.imageView.setAlpha(0.4f);
                }
            } else {
                ownedTextView.setVisibility(View.GONE);
                DataBindingUtils.loadImage(this.imageView, "PixelPaw");
                this.imageView.setAlpha(0.4f);
            }
        }

        @Override
        public void onClick(View v) {
            if (animal != null) {
                if (ownedMapping != null) {
                    if (ownedMapping.containsKey(animal.getAnimal()) && ownedMapping.get(animal.getAnimal()) > 0) {
                        if (itemType.equals("pets")) {
                            PetDetailRecyclerFragment fragment = new PetDetailRecyclerFragment();
                            fragment.animalType = animal.getAnimal();
                            activity.displayFragment(fragment);
                        } else {
                            MountDetailRecyclerFragment fragment = new MountDetailRecyclerFragment();
                            fragment.animalType = animal.getAnimal();
                            activity.displayFragment(fragment);
                        }
                    }
                }
            }
        }
    }
}
