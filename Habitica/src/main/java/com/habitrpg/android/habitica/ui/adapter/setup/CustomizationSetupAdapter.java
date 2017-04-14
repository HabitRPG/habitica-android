package com.habitrpg.android.habitica.ui.adapter.setup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.EquipCommand;
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.SetupCustomization;

import org.greenrobot.eventbus.EventBus;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomizationSetupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String userSize;
    public HabitRPGUser user;
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
        Preferences prefs = this.user.getPreferences();
        switch (customization.category) {
            case "body": {
                switch (customization.subcategory) {
                    case "size":
                        return customization.key.equals(prefs.getSize());
                    case "shirt":
                        return customization.key.equals(prefs.getShirt());
                }
            }
            case "skin":
                return customization.key.equals(prefs.getSkin());
            case "background":
                return customization.key.equals(prefs.getBackground());
            case "hair":
                switch (customization.subcategory) {
                    case "bangs":
                        return Integer.parseInt(customization.key) == prefs.getHair().getBangs();
                    case "base":
                        return Integer.parseInt(customization.key) == prefs.getHair().getBase();
                    case "color":
                        return customization.key.equals(prefs.getHair().getColor());
                    case "flower":
                        return Integer.parseInt(customization.key) == prefs.getHair().getFlower();
                    case "beard":
                        return Integer.parseInt(customization.key) == prefs.getHair().getBeard();
                    case "mustache":
                        return Integer.parseInt(customization.key) == prefs.getHair().getMustache();
                }
            case "extras": {
                switch (customization.subcategory) {
                    case "glasses":
                        return customization.key.equals(this.user.getItems().getGear().getEquipped().getEyeWear()) || ("eyewear_base_0".equals(this.user.getItems().getGear().getEquipped().getEyeWear()) && customization.key.length() == 0);
                    case "flower":
                        return Integer.parseInt(customization.key) == prefs.getHair().getFlower();
                    case "wheelchair":
                        return ("chair_"+customization.key).equals(prefs.getChair()) || customization.key.equals(prefs.getChair()) || (customization.key.equals("none") && prefs.getChair() == null);
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

            if (customization.drawableId != null) {
                imageView.setImageResource(customization.drawableId);
            } else if (customization.colorId != null) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.setup_customization_circle);
                if (drawable != null) {
                    drawable.setColorFilter(ContextCompat.getColor(context, customization.colorId), PorterDuff.Mode.MULTIPLY);
                }
                imageView.setImageDrawable(drawable);
            } else {
                imageView.setImageDrawable(null);
            }
            textView.setText(customization.text);
            if (!"0".equals(customization.key) && "flower".equals(customization.subcategory)) {
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
            if (customization.path.equals("glasses")) {
                EquipCommand command = new EquipCommand();
                if (customization.key.length() == 0) {
                    command.key = user.getItems().getGear().getEquipped().getEyeWear();
                } else {
                    command.key = customization.key;
                }
                command.type = "equipped";
                EventBus.getDefault().post(command);
            } else {
                UpdateUserCommand command = new UpdateUserCommand();
                Map<String, Object> updateData = new HashMap<>();
                String updatePath = "preferences." + customization.getPath();
                updateData.put(updatePath, customization.key);

                command.updateData = updateData;

                EventBus.getDefault().post(command);
            }
        }
    }
}
