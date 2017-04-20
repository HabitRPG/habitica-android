package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

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
