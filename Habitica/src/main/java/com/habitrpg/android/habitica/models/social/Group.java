package com.habitrpg.android.habitica.models.social;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

@Table(databaseName = HabitDatabase.NAME, tableName = "_group")
public class Group extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    @SerializedName("_id")
    public String id;

    @Column
    public double balance;

    @Column
    public String description;

    @Column
    public String leaderID;

    @Column
    public String leaderName;

    @Column
    public String name;

    @Column
    public int memberCount;

    @Column
    public Boolean isMember;

    @Column
    public String type;

    @Column
    public String logo;

    public Quest quest;

    public String privacy;


    public List<ChatMessage> chat;

    public List<HabitRPGUser> members;

    @Column
    public int challengeCount;

    @Column
    public String leaderMessage;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Group group = (Group) o;

        return id != null ? id.equals(group.id) : group.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
