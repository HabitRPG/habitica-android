package com.magicmicky.habitrpgwrapper.lib.models.inventory;


import com.habitrpg.android.habitica.R;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SpecialItem extends Item {

    public boolean isMysteryItem;

    public static Item makeMysteryItem(Context context) {
        SpecialItem item = new SpecialItem();
        item.text = context.getString(R.string.mystery_item);
        item.notes = context.getString(R.string.myster_item_notes);
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        String month = sdf.format(new Date());
        item.key = "inventory_present_" + month;
        item.isMysteryItem = true;
        return item;
    }

    @Override
    public String getType() {
        return "special";
    }
}
