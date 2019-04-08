package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmModel;

public interface Item extends RealmModel {

    String getType();

    String getKey();

    String getText();

    Integer getValue();
}
