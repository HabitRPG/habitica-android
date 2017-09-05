package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by phillip on 25.07.17.
 */

public class QuestDropItem extends RealmObject {

    @PrimaryKey
    private String combinedKey;
    private String questKey;
    private String key;
    private String type;
    private String text;
    private boolean onlyOwner;
    private int count;

    public QuestDropItem() {
        super();
    }

    public String getCombinedKey() {
        return combinedKey;
    }

    public void setCombinedKey(String combinedKey) {
        this.combinedKey = combinedKey;
    }

    public String getQuestKey() {
        return questKey;
    }

    public void setQuestKey(String questKey) {
        this.questKey = questKey;
        combinedKey = questKey+key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        combinedKey = questKey+key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getImageName() {
        if ("quests".equals(type)) {
            return "inventory_quest_scroll_"+key;
        } else if ("eggs".equals(type)) {
            return "Pet_Egg_" + getKey();
        } else
        if ("food".equals(type)) {
            return "Pet_Food_" + getKey();
        } else
        if ("hatchingPotions".equals(type)) {
            return "Pet_HatchingPotion_" + getKey();
        }
        return "shop_"+key;
    }

    public boolean isOnlyOwner() {
        return onlyOwner;
    }

    public void setOnlyOwner(boolean onlyOwner) {
        this.onlyOwner = onlyOwner;
    }
}
