package com.habitrpg.android.habitica.ui.adapter.inventory;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EquipmentRecyclerViewAdapter extends RecyclerView.Adapter<EquipmentRecyclerViewAdapter.GearViewHolder> {

private List<ItemData> gearList;

public String equippedGear;
public Boolean isCostume;
public String type;

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

    @BindView(R.id.gear_container)
    View gearContainer;

    @BindView(R.id.gear_text)
    TextView gearNameTextView;

    @BindView(R.id.gear_notes)
    TextView gearNotesTextView;

    @BindView(R.id.gear_image)
    ImageView imageView;

    @BindView(R.id.equippedIndicator)
    View equippedIndicator;

    ItemData gear;

    Context context;

    public GearViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        context = itemView.getContext();
        itemView.setOnClickListener(this);
    }

    public void bind(ItemData gear) {
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

        Picasso.with(imageView.getContext())
                .load(imageUrl)
                .into(imageView);
    }

    @Override
    public void onClick(View v) {
        EquipCommand command = new EquipCommand();
        command.key = this.gear.key;
        if (isCostume) {
            command.type = "costume";
        } else {
            command.type = "equipped";
        }
        EventBus.getDefault().post(command);
        if (this.gear.key.equals(equippedGear)) {
            equippedGear = type + "_base_0";
        } else {
            equippedGear = this.gear.key;
        }
        notifyDataSetChanged();
    }
}
}
