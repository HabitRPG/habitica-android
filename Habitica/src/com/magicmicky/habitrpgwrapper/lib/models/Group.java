package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;

/**
 * Created by Negue on 16.09.2015.
 */
public class Group extends BaseModel {

    @SerializedName("_id")
    public String id;

    public double balance;

    public String description;

    public String leaderID;

    public String name;

    public int memberCount;

    public Boolean isMember;

    public String type;

    public String logo;

    public Quest quest;

    public String privacy;

    public ArrayList<ChatMessage> chat;

    public ArrayList<HabitRPGUser> members;

    public int challengeCount;

    // TODO Challenges
}
