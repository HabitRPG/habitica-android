package com.habitrpg.android.habitica.ui.adapter.setup;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.models.SetupCustomization;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.user.User;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomizationSetupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String userSize;
    public User user;
    private List<SetupCustomization> customizationList;

    public void setCustomizationList(List<SetupCustomization> newCustomizationList) {
        this.customizationList = newCustomizationList;
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewID = R.layout.setup_customization_item;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewID, parent, false);

        return new CustomizationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CustomizationViewHolder) holder).bind(customizationList.get(position));
    }

    @Override
    public int getItemCount() {
        return customizationList == null ? 0 : customizationList.size();
    }

    private boolean isCustomizationActive(SetupCustomization customization) {
        if (this.user == null) {
            return false;
        }
        Preferences prefs = this.user.getPreferences();
        switch (customization.getCategory()) {
            case "body": {
                switch (customization.getSubcategory()) {
                    case "size":
                        return customization.getKey().equals(prefs.getSize());
                    case "shirt":
                        return customization.getKey().equals(prefs.getShirt());
                }
            }
            case "skin":
                return customization.getKey().equals(prefs.getSkin());
            case "background":
                return customization.getKey().equals(prefs.getBackground());
            case "hair":
                switch (customization.getSubcategory()) {
                    case "bangs":
                        return Integer.parseInt(customization.getKey()) == prefs.getHair().getBangs();
                    case "base":
                        return Integer.parseInt(customization.getKey()) == prefs.getHair().getBase();
                    case "color":
                        return customization.getKey().equals(prefs.getHair().getColor());
                    case "flower":
                        return Integer.parseInt(customization.getKey()) == prefs.getHair().getFlower();
                    case "beard":
                        return Integer.parseInt(customization.getKey()) == prefs.getHair().getBeard();
                    case "mustache":
                        return Integer.parseInt(customization.getKey()) == prefs.getHair().getMustache();
                }
            case "extras": {
                switch (customization.getSubcategory()) {
                    case "glasses":
                        return customization.getKey().equals(this.user.getItems().getGear().getEquipped().getEyeWear()) || ("eyewear_base_0".equals(this.user.getItems().getGear().getEquipped().getEyeWear()) && customization.getKey().length() == 0);
                    case "flower":
                        return Integer.parseInt(customization.getKey()) == prefs.getHair().getFlower();
                    case "wheelchair":
                        return ("chair_"+ customization.getKey()).equals(prefs.getChair()) || customization.getKey().equals(prefs.getChair()) || (customization.getKey().equals("none") && prefs.getChair() == null);
                }
            }
        }
        return false;
    }

    class CustomizationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.imageView)
        ImageView imageView;

        @BindView(R.id.textView)
        TextView textView;

        SetupCustomization customization;

        Context context;

        CustomizationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            context = itemView.getContext();
        }

        public void bind(SetupCustomization customization) {
            this.customization = customization;

            if (customization.getDrawableId() != null) {
                imageView.setImageResource(customization.getDrawableId());
            } else if (customization.getColorId() != null) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.setup_customization_circle);
                if (drawable != null) {
                    drawable.setColorFilter(ContextCompat.getColor(context, customization.getColorId()), PorterDuff.Mode.MULTIPLY);
                }
                imageView.setImageDrawable(drawable);
            } else {
                imageView.setImageDrawable(null);
            }
            textView.setText(customization.getText());
            if (!"0".equals(customization.getKey()) && "flower".equals(customization.getSubcategory())) {
                if (isCustomizationActive(customization)) {
                    imageView.setBackgroundResource(R.drawable.setup_customization_flower_bg_selected);
                } else {
                    imageView.setBackgroundResource(R.drawable.setup_customization_flower_bg);
                }
            } else {
                if (isCustomizationActive(customization)) {
                    imageView.setBackgroundResource(R.drawable.setup_customization_bg_selected);
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    imageView.setBackgroundResource(R.drawable.setup_customization_bg);
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white_50_alpha));
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (customization.getPath().equals("glasses")) {
                EquipCommand command = new EquipCommand();
                if (customization.getKey().length() == 0) {
                    command.key = user.getItems().getGear().getEquipped().getEyeWear();
                } else {
                    command.key = customization.getKey();
                }
                command.type = "equipped";
                EventBus.getDefault().post(command);
            } else {
                UpdateUserCommand command = new UpdateUserCommand();
                Map<String, Object> updateData = new HashMap<>();
                String updatePath = "preferences." + customization.getPath();
                updateData.put(updatePath, customization.getKey());

                command.updateData = updateData;

                EventBus.getDefault().post(command);
            }
        }
    }
}
