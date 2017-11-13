package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.models.members.Member;

import io.realm.RealmList;
import io.realm.RealmObject;
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

    public RealmList<Member> participants;

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