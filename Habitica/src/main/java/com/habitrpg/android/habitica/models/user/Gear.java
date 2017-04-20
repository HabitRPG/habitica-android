package com.habitrpg.android.habitica.models.user;

import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Gear extends RealmObject {

    @Ignore
    public HashMap<String, Boolean> owned;
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
}
