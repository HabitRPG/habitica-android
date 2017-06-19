package com.habitrpg.android.habitica.models.inventory;

import java.util.HashMap;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Quest extends RealmObject {
    @PrimaryKey
    public String id;
    public String key;
    public boolean active;
    public String leader;
    public boolean RSVPNeeded;

    public RealmList<QuestMember> members;
    private QuestProgress progress;

    private Quest(String key, QuestProgress progress) {
        this.key = key;
        this.progress = progress;
    }

    public Quest() {
    }

    public QuestProgress getProgress() {
        return progress;
    }

    public void setProgress(QuestProgress progress) {
        this.progress = progress;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}