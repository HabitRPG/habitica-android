package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.HabitDatabase;
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
    private String order;//Order to display ppl

    @Column
    private String orderAscending;//Order type

    public UserParty() {

    }

    public UserParty(String id, Quest quest, String order, String orderAscending) {
        this.id = id;
        this.quest = quest;
        this.order = order;
        this.orderAscending = orderAscending;
    }


    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
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
