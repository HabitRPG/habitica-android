package com.habitrpg.android.habitica.ui.adapter;

import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.raizlabs.android.dbflow.sql.language.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by viirus on 13/01/16.
 */
public class CustomizationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> customizationList;
    private String activeCustomization;
    public String userSize;
    public String hairColor;

    public void setCustomizationList(List<Customization> newCustomizationList) {
        this.customizationList = new ArrayList<Object>();
        String lastSetName = null;
        for (Customization customization : newCustomizationList) {
            if (customization.getSet() != null && !customization.getSet().equals(lastSetName)) {
                customizationList.add(customization.getSet());
                lastSetName = customization.getSet();
            }
            customizationList.add(customization);
        }
        this.notifyDataSetChanged();
    }

    public void setActiveCustomization(String activeCustomization) {
        this.activeCustomization = activeCustomization;
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
                    .inflate(R.layout.customization_grid_item, parent, false);

            return new CustomizationViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = customizationList.get(position);
        if (obj.getClass().equals(String.class)) {
            ((SectionViewHolder)holder).bind((String) obj);
        } else {
            ((CustomizationViewHolder)holder).bind((Customization) customizationList.get(position));

        }
    }

    @Override
    public int getItemCount() {
        return customizationList == null ? 0 : customizationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (this.customizationList.get(position).getClass().equals(String.class)) {
            return 0;
        } else {
            return 1;
        }
    }

    class CustomizationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.card_view)
        CardView cardView;

        @Bind(R.id.linearLayout)
        LinearLayout linearLayout;

        @Bind(R.id.imageView)
        ImageView imageView;

        Customization customization;

        public CustomizationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            linearLayout.setOnClickListener(this);
        }

        public void bind(Customization customization) {
            this.customization = customization;


            DataBindingUtils.loadImage(this.imageView, customization.getImageName(userSize, hairColor));
            cardView.setCardBackgroundColor(android.R.color.white);
            if (customization.isUsable()) {
                imageView.setAlpha(1.0f);
                if (customization.getIdentifier().equals(activeCustomization)) {
                    cardView.setCardBackgroundColor(R.color.brand_500);
                }
            } else {
                imageView.setAlpha(0.3f);
            }
        }

        @Override
        public void onClick(View v) {
            if (!customization.isUsable() && customization.getIdentifier().equals(activeCustomization)) {
                return;
            }

            UpdateUserCommand command = new UpdateUserCommand();
            Map<String, String> updateData = new HashMap<String, String>();
            String updatePath = "preferences." + customization.getType();
            if (customization.getGroup() != null) {
                updatePath = updatePath + "." + customization.getGroup();
            }
            updateData.put(updatePath, customization.getIdentifier());
            command.updateData = updateData;

            EventBus.getDefault().post(command);
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        private String sectionName;

        @Bind(R.id.label)
        TextView label;

        public SectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(String sectionName) {
            this.sectionName = sectionName;
            String uppercasedSectionName = sectionName.substring(0, 1).toUpperCase() + sectionName.substring(1);
            this.label.setText(uppercasedSectionName);
        }
    }
}
