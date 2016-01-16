package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.events.commands.UnlockPathCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;

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
    public double gemBalance;

    public void setCustomizationList(List<Customization> newCustomizationList) {
        this.customizationList = new ArrayList<Object>();
        String lastSetName = null;
        for (Customization customization : newCustomizationList) {
            if (customization.getCustomizationSet() != null && !customization.getCustomizationSet().equals(lastSetName)) {
                customizationList.add(customization.getCustomizationSet());
                lastSetName = customization.getCustomizationSet();
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
        RelativeLayout linearLayout;

        @Bind(R.id.imageView)
        ImageView imageView;

        @Bind(R.id.purchaseOverlay)
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


            DataBindingUtils.loadImage(this.imageView, customization.getImageName(userSize, hairColor));
            cardView.setCardBackgroundColor(android.R.color.white);
            if (customization.isUsable()) {
                imageView.setAlpha(1.0f);
                purchaseOverlay.setAlpha(0.0f);
                if (customization.getIdentifier().equals(activeCustomization)) {
                    cardView.setCardBackgroundColor(R.color.brand_500);
                }
            } else {
                imageView.setAlpha(0.3f);
                purchaseOverlay.setAlpha(0.8f);
            }
        }

        @Override
        public void onClick(View v) {
            if (!customization.isUsable()) {

                LinearLayout dialogContent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_purchase_customization, null);

                ImageView imageView = (ImageView) dialogContent.findViewById(R.id.imageView);
                DataBindingUtils.loadImage(imageView, customization.getImageName(userSize, hairColor));

                TextView priceLabel = (TextView) dialogContent.findViewById(R.id.priceLabel);
                priceLabel.setText(String.valueOf(customization.getPrice()));

                MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    if (customization.getPrice() > gemBalance) {
                                        OpenMenuItemCommand event = new OpenMenuItemCommand();
                                        event.identifier = MainDrawerBuilder.SIDEBAR_PURCHASE;
                                        EventBus.getDefault().post(event);
                                        return;
                                    }
                                UnlockPathCommand event = new UnlockPathCommand();
                                event.path = customization.getPath();
                                event.balanceDiff = customization.getPrice() / 4;
                                EventBus.getDefault().post(event);
                            }
                        })
                        .contentGravity(GravityEnum.CENTER)
                        .positiveColor(context.getResources().getColor(R.color.brand_200))
                        .positiveText(R.string.reward_dialog_buy)
                        .title(context.getString(R.string.purchase_customization))
                        .customView(dialogContent, true)
                        .negativeText(R.string.reward_dialog_dismiss)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                materialDialog.dismiss();
                            }
                        }).build();
                dialog.show();
                return;
            }

            if (customization.getIdentifier().equals(activeCustomization)) {
                return;
            }

            UpdateUserCommand command = new UpdateUserCommand();
            Map<String, String> updateData = new HashMap<String, String>();
            String updatePath = "preferences." + customization.getType();
            if (customization.getCategory() != null) {
                updatePath = updatePath + "." + customization.getCategory();
            }
            updateData.put(updatePath, customization.getIdentifier());
            command.updateData = updateData;

            EventBus.getDefault().post(command);
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String sectionName;

        @Bind(R.id.label)
        TextView label;

        @Bind(R.id.purchaseSetButton)
        Button purchaseSetButton;

        Context context;

        public SectionViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            ButterKnife.bind(this, itemView);
            purchaseSetButton.setOnClickListener(this);
        }

        public void bind(String sectionName) {
            this.sectionName = sectionName;
            String uppercasedSectionName = sectionName.substring(0, 1).toUpperCase() + sectionName.substring(1);
            this.label.setText(uppercasedSectionName);
            this.purchaseSetButton.setText(context.getString(R.string.purchase_set_button, 5));
        }

        @Override
        public void onClick(View v) {
                LinearLayout dialogContent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_purchase_customization, null);

                TextView priceLabel = (TextView) dialogContent.findViewById(R.id.priceLabel);
                priceLabel.setText("5");

                MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                if (5 > gemBalance) {
                                    OpenMenuItemCommand event = new OpenMenuItemCommand();
                                    event.identifier = MainDrawerBuilder.SIDEBAR_PURCHASE;
                                    EventBus.getDefault().post(event);
                                    return;
                                }
                                UnlockPathCommand event = new UnlockPathCommand();
                                String path = "";
                                for (Object obj : customizationList) {
                                    if (obj.getClass().equals(Customization.class)) {
                                        Customization customization = (Customization) obj;
                                        if (customization.getCustomizationSet() != null && customization.getCustomizationSet().equals(sectionName)) {
                                            path = path + "," + customization.getPath();
                                        }
                                    }
                                }
                                event.path = path;
                                event.balanceDiff = 1.25;
                                EventBus.getDefault().post(event);
                            }
                        })
                        .contentGravity(GravityEnum.CENTER)
                        .positiveColor(context.getResources().getColor(R.color.brand_200))
                        .positiveText(R.string.reward_dialog_buy)
                        .title(context.getString(R.string.purchase_customization))
                        .customView(dialogContent, true)
                        .negativeText(R.string.reward_dialog_dismiss)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                materialDialog.dismiss();
                            }
                        }).build();
                dialog.show();
        }
    }
}
