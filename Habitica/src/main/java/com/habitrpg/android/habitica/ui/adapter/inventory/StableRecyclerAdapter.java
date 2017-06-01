package com.habitrpg.android.habitica.ui.adapter.inventory;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.MountDetailRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.PetDetailRecyclerFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder;
import com.habitrpg.android.habitica.models.inventory.Animal;

import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StableRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String itemType;
    public MainActivity activity;
    private List<Object> itemList;

    public <T extends Animal> void setItemList(List<Object> itemList) {
        this.itemList = itemList;
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.customization_section_header, parent, false);

            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.animal_overview_item, parent, false);

            return new StableViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = this.itemList.get(position);
        if (obj.getClass().equals(String.class)) {
            ((SectionViewHolder) holder).bind((String) obj);
        } else {
            ((StableViewHolder) holder).bind((Animal) itemList.get(position));

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (this.itemList.get(position).getClass().equals(String.class)) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class StableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Animal animal;

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.imageView)
        SimpleDraweeView imageView;

        @BindView(R.id.titleTextView)
        TextView titleView;

        @BindView(R.id.ownedTextView)
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
            titleView.setText(item.getAnimalText());
            ownedTextView.setVisibility(View.VISIBLE);
            this.imageView.setAlpha(1.0f);
            if (animal.getNumberOwned() > 0) {
                this.ownedTextView.setText(animal.getNumberOwned().toString());
                if (itemType.equals("pets")) {
                    DataBindingUtils.loadImage(this.imageView, "Pet-" + item.getKey());
                } else {
                    DataBindingUtils.loadImage(this.imageView, "Mount_Icon_" + item.getKey());
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
                if (animal.getNumberOwned() > 0) {
                    if (itemType.equals("pets")) {
                        PetDetailRecyclerFragment fragment = new PetDetailRecyclerFragment();
                        fragment.animalType = animal.getAnimal();
                        fragment.animalGroup = animal.getAnimalGroup();
                        activity.displayFragment(fragment);
                    } else {
                        MountDetailRecyclerFragment fragment = new MountDetailRecyclerFragment();
                        fragment.animalType = animal.getAnimal();
                        fragment.animalGroup = animal.getAnimalGroup();
                        activity.displayFragment(fragment);
                    }
                }
            }
        }
    }
}
