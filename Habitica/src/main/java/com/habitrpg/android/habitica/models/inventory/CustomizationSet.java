package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.shared.habitica.models.inventory.Customization;

import java.util.List;

/**
 * Created by viirus on 19/01/16.
 */
public class CustomizationSet {

    public String text;
    public String identifier;
    public Integer price;
    public boolean hasPurchasable;

    public List<Customization> customizations;
}
