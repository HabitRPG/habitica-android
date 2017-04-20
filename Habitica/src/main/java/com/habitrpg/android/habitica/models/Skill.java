package com.habitrpg.android.habitica.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Skill extends RealmObject {

    @PrimaryKey
    public String key;
    public String text, notes, target, habitClass;
    public Integer mana, lvl;
    public boolean isSpecialItem;
}
