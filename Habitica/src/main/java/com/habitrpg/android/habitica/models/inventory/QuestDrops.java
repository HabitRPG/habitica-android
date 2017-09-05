package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by phillip on 25.07.17.
 */

public class QuestDrops extends RealmObject {

    @PrimaryKey
    private String key;
    public int gp;
    public int exp;
    public String unlock;
    private RealmList<QuestDropItem> items;

    public QuestDrops() {
        super();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;

        if (items != null) {
            for (QuestDropItem item : items) {
                item.setQuestKey(key);
            }
        }
    }


    public RealmList<QuestDropItem> getItems() {
        return items;
    }

    public void setItems(RealmList<QuestDropItem> items) {
        this.items = items;

        if (items != null) {
            for (QuestDropItem item : items) {
                item.setQuestKey(key);
            }
        }
    }
}
