package com.habitrpg.android.habitica.ui.adapter.setup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomizationSetupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String userSize;
    public HabitRPGUser user;
    private List<Object> customizationList;

    public void setCustomizationList(List<Customization> newCustomizationList) {
        this.customizationList = new ArrayList<>();
        String lastSectionTitle = "";
        for (Customization customization : newCustomizationList) {
            String sectionTitle = customization.getType();
            if (customization.getCategory() != null) {
                if (customization.getCategory().equals("mustache") || customization.getCategory().equals("beard")) {
                    continue;
                }
                sectionTitle = sectionTitle + " - " + customization.getCategory();
            }
            if (!sectionTitle.equals(lastSectionTitle)) {
                lastSectionTitle = sectionTitle;
                customizationList.add(sectionTitle);
            }
            customizationList.add(customization);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.customization_section_header, parent, false);

            return new SectionViewHolder(view);
        } else {
            int viewID = R.layout.customization_grid_item;

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewID, parent, false);

            return new CustomizationViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = customizationList.get(position);
        if (obj.getClass().equals(String.class)) {
            ((SectionViewHolder) holder).bind((String) obj);
        } else {
            ((CustomizationViewHolder) holder).bind((Customization) customizationList.get(position));

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

    private boolean isCustomizationActive(Customization customization) {
        Preferences prefs = this.user.getPreferences();
        switch (customization.getType()) {
            case "skin":
                return customization.getIdentifier().equals(prefs.getSkin());
            case "shirt":
                return customization.getIdentifier().equals(prefs.getShirt());
            case "background":
                return customization.getIdentifier().equals(prefs.getBackground());
            case "hair":
                switch (customization.getCategory()) {
                    case "bangs":
                        return Integer.parseInt(customization.getIdentifier()) == prefs.getHair().getBangs();
                    case "base":
                        return Integer.parseInt(customization.getIdentifier()) == prefs.getHair().getBase();
                    case "color":
                        return customization.getIdentifier().equals(prefs.getHair().getColor());
                    case "flower":
                        return Integer.parseInt(customization.getIdentifier()) == prefs.getHair().getFlower();
                    case "beard":
                        return Integer.parseInt(customization.getIdentifier()) == prefs.getHair().getBeard();
                    case "mustache":
                        return Integer.parseInt(customization.getIdentifier()) == prefs.getHair().getMustache();
                }
        }
        return false;
    }

    private String getHairColor() {
        if (this.user != null) {
            return this.user.getPreferences().getHair().getColor();
        } else {
            return "";
        }
    }

    class CustomizationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.linearLayout)
        RelativeLayout linearLayout;

        @BindView(R.id.imageView)
        ImageView imageView;

        @BindView(R.id.purchaseOverlay)
        View purchaseOverlay;

        Customization customization;

        Context context;

        public CustomizationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            linearLayout.setOnClickListener(this);

            context = itemView.getContext();
        }

        public void bind(Customization customization) {
            this.customization = customization;

            DataBindingUtils.loadImage(this.imageView, customization.getImageName(userSize, getHairColor()));
            cardView.setCardBackgroundColor(android.R.color.white);
            imageView.setAlpha(1.0f);
            purchaseOverlay.setAlpha(0.0f);
            if (isCustomizationActive(this.customization)) {
                cardView.setCardBackgroundColor(R.color.brand_500);
            }
        }

        @Override
        public void onClick(View v) {
            UpdateUserCommand command = new UpdateUserCommand();
            Map<String, Object> updateData = new HashMap<>();
            String updatePath = "preferences." + customization.getType();
            if (customization.getCategory() != null) {
                updatePath = updatePath + "." + customization.getCategory();
            }
            updateData.put(updatePath, customization.getIdentifier());
            command.updateData = updateData;

            EventBus.getDefault().post(command);
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.label)
        TextView label;

        Context context;

        public SectionViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            ButterKnife.bind(this, itemView);
        }

        public void bind(String sectionTitle) {
            this.label.setText(sectionTitle);
        }
    }
}
