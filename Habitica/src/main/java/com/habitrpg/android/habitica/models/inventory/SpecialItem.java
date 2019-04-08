package com.habitrpg.android.habitica.models.inventory;


import android.content.Context;

import com.habitrpg.android.habitica.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SpecialItem extends RealmObject implements Item {

    @PrimaryKey
    String key;
    String text, notes;
    Integer value;
    public boolean isMysteryItem;

    public static SpecialItem makeMysteryItem(Context context) {
        SpecialItem item = new SpecialItem();
        item.text = context.getString(R.string.mystery_item);
        item.notes = context.getString(R.string.myster_item_notes);
        SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String month = sdf.format(new Date());
        item.key = "inventory_present_" + month;
        item.isMysteryItem = true;
        return item;
    }

    @Override
    public String getType() {
        return "special";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
