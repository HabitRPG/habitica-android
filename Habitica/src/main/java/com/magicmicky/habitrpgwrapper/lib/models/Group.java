package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;

import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Created by Negue on 16.09.2015.
 */
public class Group extends BaseModel {

    @SerializedName("_id")
    public String id;

    public double balance;

    public String description;

    public String leaderID;

    public String leaderName;

    public String name;

    public int memberCount;

    public Boolean isMember;

    public String type;

    public String logo;

    public Quest quest;

    public String privacy;

    public List<ChatMessage> chat;

    public List<HabitRPGUser> members;

    public int challengeCount;

    public String leaderMessage;

    // TODO Challenges


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return id != null ? id.equals(group.id) : group.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
