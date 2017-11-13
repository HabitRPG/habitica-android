package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.Equipment;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import rx.Observable;
import rx.subjects.PublishSubject;

public class EquipmentRecyclerViewAdapter extends RealmRecyclerViewAdapter<Equipment, EquipmentRecyclerViewAdapter.GearViewHolder> {

    public String equippedGear;
    public Boolean isCostume;
    public String type;

    private PublishSubject<String> equipEvents = PublishSubject.create();

    public EquipmentRecyclerViewAdapter(@Nullable OrderedRealmCollection<Equipment> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }

    @Override
    public GearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gear_list_item, parent, false);

        return new GearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GearViewHolder holder, int position) {
        if (getData() != null) {
            holder.bind(getData().get(position));
        }
    }

    public Observable<String> getEquipEvents() {
        return equipEvents.asObservable();
    }

    class GearViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.gear_container)
        View gearContainer;

        @BindView(R.id.gear_text)
        TextView gearNameTextView;

        @BindView(R.id.gear_notes)
        TextView gearNotesTextView;

        @BindView(R.id.gear_image)
        SimpleDraweeView imageView;

        @BindView(R.id.equippedIndicator)
        View equippedIndicator;

        Equipment gear;

        Context context;

        public GearViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        public void bind(Equipment gear) {
            this.gear = gear;
            this.gearNameTextView.setText(this.gear.text);
            this.gearNotesTextView.setText(this.gear.notes);

            if (gear.key.equals(equippedGear)) {
                this.equippedIndicator.setVisibility(View.VISIBLE);
                this.gearContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_700));
            } else {
                this.equippedIndicator.setVisibility(View.GONE);
                this.gearContainer.setBackgroundResource(R.drawable.selection_highlight);
            }

            String imageUrl = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/shop_" + gear.key + ".png";
            imageView.setImageURI(Uri.parse(imageUrl));
        }

        @Override
        public void onClick(View v) {
            equipEvents.onNext(this.gear.key);
            if (this.gear.key.equals(equippedGear)) {
                equippedGear = type + "_base_0";
            } else {
                equippedGear = this.gear.key;
            }
            notifyDataSetChanged();
        }
    }
}
