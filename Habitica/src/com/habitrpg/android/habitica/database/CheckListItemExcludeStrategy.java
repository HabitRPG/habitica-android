package com.habitrpg.android.habitica.database;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Created by franzejr on 29/11/15.
 */
public class CheckListItemExcludeStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(ExcludeCheckListItem.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return clazz.getAnnotation(ExcludeCheckListItem.class) != null;
    }
}
