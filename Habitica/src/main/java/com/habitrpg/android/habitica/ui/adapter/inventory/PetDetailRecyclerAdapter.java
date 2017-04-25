package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
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
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class PetDetailRecyclerAdapter extends RealmRecyclerViewAdapter<Pet, PetDetailRecyclerAdapter.PetViewHolder> {

    public String itemType;
    public Context context;
    private RealmResults<Mount> ownedMounts;

    public PetDetailRecyclerAdapter(@Nullable OrderedRealmCollection<Pet> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }


    @Override
    public PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pet_detail_item, parent, false);

        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PetViewHolder holder, int position) {
        if (getData() != null) {
            holder.bind(this.getData().get(position));
        }
    }

    public void setOwnedMounts(RealmResults<Mount> ownedMounts) {
        this.ownedMounts = ownedMounts;
        notifyDataSetChanged();
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

        public void bind(Pet item) {
            this.animal = item;
            this.titleView.setText(item.getColorText());
            this.trainedProgressbar.setVisibility(animal.getAnimalGroup().equals("specialPets") ? View.GONE : View.VISIBLE);
            this.imageView.setAlpha(1.0f);
            if (this.animal.getTrained() > 0) {
                if (this.isMountOwned()) {
                    this.trainedProgressbar.setVisibility(View.GONE);
                } else {
                    this.trainedProgressbar.setProgress(this.animal.getTrained());
                }
                DataBindingUtils.loadImage(this.imageView, "Pet-" + itemType + "-" + item.getColor());
            } else {
                this.trainedProgressbar.setVisibility(View.GONE);
                if (this.animal.getTrained() == 0) {
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

        private boolean isOwned() {
            return this.animal.getTrained() > 0;
        }

        public boolean isMountOwned() {
            for (Mount ownedMount : ownedMounts) {
                if (ownedMount.getKey().equals(animal.getKey())) {
                    return true;
                }
            }
            return false;
        }
    }
}
