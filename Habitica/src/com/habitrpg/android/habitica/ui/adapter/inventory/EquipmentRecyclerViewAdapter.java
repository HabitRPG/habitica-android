package com.habitrpg.android.habitica.ui.adapter.inventory;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipGearCommand;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EquipmentRecyclerViewAdapter extends RecyclerView.Adapter<EquipmentRecyclerViewAdapter.GearViewHolder> {

private List<ItemData> gearList;

public String equippedGear;
public Boolean isCostume;

public void setGearList(List<ItemData> gearList) {
        this.gearList = gearList;
        this.notifyDataSetChanged();
        }

@Override
public GearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.gear_list_item, parent, false);

        return new GearViewHolder(view);
        }

@Override
public void onBindViewHolder(GearViewHolder holder, int position) {
        holder.bind(gearList.get(position));
        }

@Override
public int getItemCount() {
        return gearList == null ? 0 : gearList.size();
        }

class GearViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @Bind(R.id.gear_text)
    TextView gearNameTextView;

    @Bind(R.id.gear_notes)
    TextView gearNotesTextView;

    @Bind(R.id.gear_image)
    ImageView imageView;

    ItemData gear;

    Resources resources;

    public GearViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        resources = itemView.getResources();
        itemView.setOnClickListener(this);
    }

    public void bind(ItemData gear) {
        this.gear = gear;
        this.gearNameTextView.setText(this.gear.text);
        this.gearNotesTextView.setText(this.gear.notes);

        Picasso.with(imageView.getContext())
                .load("https://habitica-assets.s3.amazonaws.com/mobileApp/images/" + gear.key + ".png")
                .into(imageView);
    }

    @Override
    public void onClick(View v) {
        EquipGearCommand command = new EquipGearCommand();
        command.gear = this.gear;
        command.asCostume = isCostume;
        EventBus.getDefault().post(command);
    }
}
}
