package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.events.commands.UnlockPathCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.ui.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.CustomizationSet;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CustomizationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> customizationList;
    private String activeCustomization;
    public String userSize;
    public String hairColor;
    public double gemBalance;

    public void setCustomizationList(List<Customization> newCustomizationList) {
        this.customizationList = new ArrayList<Object>();
        CustomizationSet lastSet = new CustomizationSet();
        for (Customization customization : newCustomizationList) {
            if (customization.getCustomizationSet() != null && !customization.getCustomizationSet().equals(lastSet.identifier)) {
                CustomizationSet set = new CustomizationSet();
                set.identifier = customization.getCustomizationSet();
                set.text = customization.getCustomizationSetName();
                set.price = customization.getSetPrice();
                set.hasPurchasable = !customization.isUsable();
                lastSet = set;
                customizationList.add(set);
            }
            customizationList.add(customization);
            if (!customization.isUsable() && !lastSet.hasPurchasable) {
                lastSet.hasPurchasable = true;
            }
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
            int viewID;
            if (viewType == 1) {
                viewID = R.layout.customization_grid_item;
            } else {
                viewID = R.layout.customization_grid_background_item;
            }


            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewID, parent, false);

            return new CustomizationViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = customizationList.get(position);
        if (obj.getClass().equals(CustomizationSet.class)) {
            ((SectionViewHolder)holder).bind((CustomizationSet) obj);
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
        if (this.customizationList.get(position).getClass().equals(CustomizationSet.class)) {
            return 0;
        } else {
            Customization customization = (Customization) customizationList.get(position);
            if (customization.getType().equals("background")) {
                return 2;
            }
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

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setPositiveButton(R.string.purchase_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                        .setTitle(context.getString(R.string.purchase_customization))
                        .setView(dialogContent)
                        .setNegativeButton(R.string.reward_dialog_dismiss, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
                return;
            }

            if (customization.getIdentifier().equals(activeCustomization)) {
                return;
            }

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

    class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CustomizationSet set;

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

        public void bind(CustomizationSet set) {
            this.set = set;
            this.label.setText(set.text);
            if (set.hasPurchasable) {
                this.purchaseSetButton.setVisibility(View.VISIBLE);
                this.purchaseSetButton.setText(context.getString(R.string.purchase_set_button, set.price));
            } else {
                this.purchaseSetButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
                LinearLayout dialogContent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_purchase_customization, null);

                TextView priceLabel = (TextView) dialogContent.findViewById(R.id.priceLabel);
                priceLabel.setText(String.valueOf(set.price));

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setPositiveButton(R.string.purchase_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (set.price > gemBalance) {
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
                                        if (!customization.isUsable() && customization.getCustomizationSet() != null && customization.getCustomizationSet().equals(set.identifier)) {
                                            path = path + "," + customization.getPath();
                                        }
                                    }
                                }
                                event.path = path;
                                event.balanceDiff = set.price / 4;
                                EventBus.getDefault().post(event);
                            }
                        })
                        .setTitle(context.getString(R.string.purchase_set_title, set.text))
                        .setView(dialogContent)
                        .setNegativeButton(R.string.reward_dialog_dismiss, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
        }
    }
}
