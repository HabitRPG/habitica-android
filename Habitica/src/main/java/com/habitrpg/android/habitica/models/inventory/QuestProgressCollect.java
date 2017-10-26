package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestProgressCollect extends RealmObject {

    @PrimaryKey
    public
    String key;

    public int count;
}
