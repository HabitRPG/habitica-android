package com.habitrpg.android.habitica.models.inventory;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import io.realm.RealmModel;

public interface Item extends RealmModel {

    String getType();

    String getKey();

    void setOwned(int size);

    String getText();

    Integer getOwned();

    Integer getValue();
}
