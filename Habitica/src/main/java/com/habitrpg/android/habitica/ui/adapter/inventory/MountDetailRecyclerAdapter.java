package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu;
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import rx.Observable;
import rx.subjects.PublishSubject;

public class MountDetailRecyclerAdapter extends RealmRecyclerViewAdapter<Mount, MountDetailRecyclerAdapter.MountViewHolder> {

    public String itemType;
    public Context context;

    private PublishSubject<String> equipEvents = PublishSubject.create();

    public MountDetailRecyclerAdapter(@Nullable OrderedRealmCollection<Mount> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }


    public Observable<String> getEquipEvents() {
        return equipEvents.asObservable();
    }

    @Override
    public MountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.animal_overview_item, parent, false);

        return new MountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MountViewHolder holder, int position) {
        if (getData() != null) {
            holder.bind(this.getData().get(position));
        }
    }

    class MountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Mount animal;

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.imageView)
        SimpleDraweeView imageView;

        @BindView(R.id.titleTextView)
        TextView titleView;

        @BindView(R.id.ownedTextView)
        TextView ownedTextView;

        Resources resources;

        MountViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            resources = itemView.getResources();

            itemView.setOnClickListener(this);
        }

        public void bind(Mount item) {
            animal = item;
            titleView.setText(item.getColorText());
            ownedTextView.setVisibility(View.GONE);
            this.imageView.setAlpha(1.0f);
            if (this.animal.getOwned()) {
                DataBindingUtils.loadImage(this.imageView, "Mount_Icon_" + itemType + "-" + item.getColor());
            } else {
                DataBindingUtils.loadImage(this.imageView, "PixelPaw");
                this.imageView.setAlpha(0.3f);
            }
        }

        @Override
        public void onClick(View v) {
            if (!this.animal.getOwned()) {
                return;
            }
            BottomSheetMenu menu = new BottomSheetMenu(context);
            menu.addMenuItem(new BottomSheetMenuItem(resources.getString(R.string.use_animal)));
            menu.setSelectionRunnable(index -> equipEvents.onNext(animal.getKey()));
            menu.show();
        }
    }
}
