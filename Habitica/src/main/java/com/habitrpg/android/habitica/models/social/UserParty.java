package com.habitrpg.android.habitica.models.social;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.inventory.Quest;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserParty extends RealmObject {
    @PrimaryKey
    @SerializedName("_id")
    public String id; //id
    private Quest quest;
    @SerializedName("order")
    private String partyOrder;//Order to display ppl
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
