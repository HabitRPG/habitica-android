package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.models.inventory.Equipment;

import java.util.HashMap;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Gear extends RealmObject {

    @PrimaryKey
    private String userId;

    public RealmList<Equipment> owned;
    Items items;
    private Outfit equipped;
    private Outfit costume;

    public Outfit getCostume() {
        return costume;
    }

    public void setCostume(Outfit costume) {
        this.costume = costume;
    }

    public Outfit getEquipped() {
        return equipped;
    }

    public void setEquipped(Outfit equipped) {
        this.equipped = equipped;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (costume != null && !costume.isManaged()) {
            costume.setUserId(userId+"costume");
        }
        if (equipped != null && !equipped.isManaged()) {
            equipped.setUserId(userId+"equipped");
        }
    }
}
