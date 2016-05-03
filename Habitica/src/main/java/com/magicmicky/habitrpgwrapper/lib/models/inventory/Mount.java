package com.magicmicky.habitrpgwrapper.lib.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class Mount extends Animal {

    @Column
    Boolean owned;

    public Boolean getOwned() {
        return owned;
    }

    public void setOwned(Boolean owned) {
        this.owned = owned;
    }
}
