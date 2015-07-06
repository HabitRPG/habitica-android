package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME)
public class Party extends BaseModel {

    @Column
    @PrimaryKey
    public String id; //id
    @Column
    private String invitation;
    @Column
    private String lastMessageSeen;
    @Column
    private boolean leader;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "quest_id",
            columnType = String.class,
            foreignColumnName = "key")})
    private Quest quest;
    @Column
    private String order;//Order to display ppl

    public Party() {

    }

    public Party(String id, String invitation, String lastMessageSeen, boolean leader, Quest quest, String order) {
        this.id = id;
        this.invitation = invitation;
        this.lastMessageSeen = lastMessageSeen;
        this.leader = leader;
        this.quest = quest;
        this.order = order;
    }

    public String getInvitation() {
        return invitation;
    }

    public void setInvitation(String invitation) {
        this.invitation = invitation;
    }

    public String getLastMessageSeen() {
        return lastMessageSeen;
    }

    public void setLastMessageSeen(String lastMessageSeen) {
        this.lastMessageSeen = lastMessageSeen;
    }

    public boolean getLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

}
