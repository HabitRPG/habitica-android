package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Negue on 29.09.2015.
 */
@Table(databaseName = HabitDatabase.NAME)
public class QuestContent extends BaseModel {
    @PrimaryKey
    @Column
    public String key;

    @Column
    public String text;

    @Column
    public String notes;

    @Column
    public double value;

    @Column
    public String previous;

    @Column
    public int lvl;

    @Column
    public boolean canBuy;

    @Column
    public String category;

    public QuestBoss boss;

    // todo drops
}

