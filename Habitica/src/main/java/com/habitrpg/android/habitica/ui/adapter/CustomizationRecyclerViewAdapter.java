package com.habitrpg.android.habitica.ui.adapter;

import android.content.Context;
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

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.CustomizationSet;
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

public class CustomizationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String userSize;
    public String hairColor;
    public double gemBalance;
    private List<Object> customizationList;
    private String activeCustomization;

    private PublishSubject<Customization> selectCustomizationEvents = PublishSubject.create();
    private PublishSubject<Customization> unlockCustomizationEvents = PublishSubject.create();
    private PublishSubject<CustomizationSet> unlockSetEvents = PublishSubject.create();

    public void updateOwnership(List<String> ownedCustomizations) {
        int position = 0;
        for (Object obj : customizationList) {
            if (obj.getClass().equals(Customization.class)) {
                Customization customization = (Customization) obj;
                if (customization.getPurchased() != ownedCustomizations.contains(customization.getId())) {
                    customization.setPurchased(ownedCustomizations.contains(customization.getId()));
                    notifyItemChanged(position);
                }
            }
            position++;
        }
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
            ((SectionViewHolder) holder).bind((CustomizationSet) obj);
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

    public List<Object> getCustomizationList() {
        return this.customizationList;
    }

    public void setCustomizationList(List<Customization> newCustomizationList) {
        this.customizationList = new ArrayList<>();
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

    public Observable<Customization> getSelectCustomizationEvents() {
        return selectCustomizationEvents.asObservable();
    }

    public Observable<Customization> getUnlockCustomizationEvents() {
        return unlockCustomizationEvents.asObservable();
    }

    public Observable<CustomizationSet> getUnlockSetEvents() {
        return unlockSetEvents.asObservable();
    }

    class CustomizationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.linearLayout)
        RelativeLayout linearLayout;

        @BindView(R.id.imageView)
        SimpleDraweeView imageView;

        @BindView(R.id.purchaseOverlay)
        View purchaseOverlay;

        Customization customization;

        Context context;

        CustomizationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            linearLayout.setOnClickListener(this);

            context = itemView.getContext();
        }

        public void bind(Customization customization) {
            this.customization = customization;


            DataBindingUtils.INSTANCE.loadImage(this.imageView, customization.getImageName(userSize, hairColor));
            cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            if (customization.isUsable()) {
                imageView.setAlpha(1.0f);
                purchaseOverlay.setAlpha(0.0f);
                if (customization.getIdentifier().equals(activeCustomization)) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(R.color.brand_500));
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

                SimpleDraweeView imageView = (SimpleDraweeView) dialogContent.findViewById(R.id.imageView);
                DataBindingUtils.INSTANCE.loadImage(imageView, customization.getImageName(userSize, hairColor));

                TextView priceLabel = (TextView) dialogContent.findViewById(R.id.priceLabel);
                priceLabel.setText(String.valueOf(customization.getPrice()));

                ((ImageView)dialogContent.findViewById(R.id.gem_icon)).setImageBitmap(HabiticaIconsHelper.imageOfGem());

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setPositiveButton(R.string.purchase_button, (dialog1, which) -> {
                            if (customization.getPrice() > gemBalance) {
                                OpenMenuItemCommand event = new OpenMenuItemCommand();
                                event.identifier = NavigationDrawerFragment.SIDEBAR_PURCHASE;
                                EventBus.getDefault().post(event);
                                return;
                            }
                            unlockCustomizationEvents.onNext(customization);
                        })
                        .setTitle(context.getString(R.string.purchase_customization))
                        .setView(dialogContent)
                        .setNegativeButton(R.string.reward_dialog_dismiss, (dialog1, which) -> dialog1.dismiss()).create();
                dialog.show();
                return;
            }

            if (customization.getIdentifier().equals(activeCustomization)) {
                return;
            }

            selectCustomizationEvents.onNext(customization);
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.label)
        TextView label;
        @BindView(R.id.purchaseSetButton)
        Button purchaseSetButton;
        Context context;
        private CustomizationSet set;

        SectionViewHolder(View itemView) {
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
                    .setPositiveButton(R.string.purchase_button, (dialog1, which) -> {
                        if (set.price > gemBalance) {
                            OpenMenuItemCommand event = new OpenMenuItemCommand();
                            event.identifier = NavigationDrawerFragment.SIDEBAR_PURCHASE;
                            EventBus.getDefault().post(event);
                            return;
                        }
                        set.customizations = new ArrayList<>();
                        for (Object obj : customizationList) {
                            if (Customization.class.isAssignableFrom(obj.getClass())) {
                                Customization customization = (Customization) obj;
                                if (!customization.isUsable() && customization.getCustomizationSet() != null && customization.getCustomizationSet().equals(set.identifier)) {
                                    set.customizations.add(customization);
                                }
                            }
                        }
                        unlockSetEvents.onNext(set);
                    })
                    .setTitle(context.getString(R.string.purchase_set_title, set.text))
                    .setView(dialogContent)
                    .setNegativeButton(R.string.reward_dialog_dismiss, (dialog1, which) -> dialog1.dismiss()).create();
            dialog.show();
        }
    }
}
