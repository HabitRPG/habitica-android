package com.habitrpg.android.habitica.models.inventory;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

public interface Item {

    String getType();

    String getKey();

    void setOwned(int size);

    String getText();

    Integer getOwned();

    Integer getValue();
}
