package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(databaseName = HabitDatabase.NAME, allFields = true)
public class Skill extends BaseModel {

    @Column
    @PrimaryKey()
    public String key;

    @Column
    public String text, notes, target, habitClass;

    @Column
    public Integer mana, lvl;

    @Column
    public Double gold;


}
