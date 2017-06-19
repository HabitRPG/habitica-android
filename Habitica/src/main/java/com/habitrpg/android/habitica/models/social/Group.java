package com.habitrpg.android.habitica.models.social;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.user.User;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Group extends RealmObject {

    @SerializedName("_id")
    @PrimaryKey
    public String id;

    public double balance;

    public String description;

    public String leaderID;

    public String leaderName;

    public String name;

    public int memberCount;

    public boolean isMember;

    public String type;

    public String logo;

    public Quest quest;

    public String privacy;

    public RealmList<ChatMessage> chat;

    public RealmList<User> members;

    public int challengeCount;

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
