package com.magicmicky.habitrpgwrapper.lib.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class Pet extends Animal {


    @Column
    Integer trained;

    public Integer getTrained() {
        return trained;
    }

    public void setTrained(Integer trained) {
        this.trained = trained;
    }
}
