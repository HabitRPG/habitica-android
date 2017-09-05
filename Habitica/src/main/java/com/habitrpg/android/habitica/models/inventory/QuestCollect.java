package com.habitrpg.android.habitica.models.inventory;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestCollect extends RealmObject {

    @PrimaryKey
    public String key;
    public String text;
    public int count;
}
