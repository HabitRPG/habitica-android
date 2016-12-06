package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.HashMap;

@Table(databaseName = HabitDatabase.NAME)
public class Challenge extends BaseModel {

    @NotNull
    @PrimaryKey
    @Column
    public String id;

    @Column
    public String name;

    @Column
    public String shortName;

    @Column
    public String description;

    public HabitRPGUser leader;

    @Column
    public String leaderName;

    public Group group;

    public int prize;

    @Column
    public boolean official;

    public HashMap<String, String[]> tasksOrder;

    @Column
    public int memberCount;

    @Column
    public String user_id;
}
