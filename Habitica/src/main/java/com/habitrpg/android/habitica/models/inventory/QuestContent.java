package com.habitrpg.android.habitica.models.inventory;

import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuestContent extends RealmObject implements Item {

    @PrimaryKey
    String key;
    String text, notes;
    int value, owned;
    public String previous;
    public int lvl;
    public boolean canBuy;
    public String category;
    public QuestBoss boss;

    RealmList<QuestCollect> collect;

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public int getLvl() {
        return lvl;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }

    public boolean isCanBuy() {
        return canBuy;
    }

    public void setCanBuy(boolean canBuy) {
        this.canBuy = canBuy;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public QuestBoss getBoss() {
        return boss;
    }

    public void setBoss(QuestBoss boss) {
        this.boss = boss;
    }

    public RealmList<QuestCollect> getCollect() {
        return collect;
    }

    public void setCollect(RealmList<QuestCollect> collect) {
        this.collect = collect;
    }

    @Override
    public String getType() {
        return "quests";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setOwned(int size) {
        owned = size;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Integer getOwned() {
        return owned;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotes() {
        return notes;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Nullable
    public QuestCollect getCollectWithKey(String key) {
        for (QuestCollect collect : this.collect) {
            if (collect.key.equals(key)) {
                return collect;
            }
        }
        return null;
    }
}
