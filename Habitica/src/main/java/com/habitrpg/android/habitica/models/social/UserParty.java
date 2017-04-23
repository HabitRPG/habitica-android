package com.habitrpg.android.habitica.models.social;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Negue on 16.09.2015.
 */
@Table(databaseName = HabitDatabase.NAME)
public class UserParty extends BaseModel {
    @Column
    @PrimaryKey
    @SerializedName("_id")
    public String id; //id

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "quest_id",
            columnType = String.class,
            foreignColumnName = "key")})
    private Quest quest;

    @Column
    @SerializedName("order")
    private String partyOrder;//Order to display ppl

    @Column
    private String orderAscending;//Order type

    public UserParty() {

    }

    public UserParty(String id, Quest quest, String partyOrder, String orderAscending) {
        this.id = id;
        this.quest = quest;
        this.partyOrder = partyOrder;
        this.orderAscending = orderAscending;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartyOrder() {
        return partyOrder;
    }

    public void setPartyOrder(String partyOrder) {
        this.partyOrder = partyOrder;
    }

    public String getOrderAscending() {
        return orderAscending;
    }

    public void setOrderAscending(String order) {
        this.orderAscending = order;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }
}
