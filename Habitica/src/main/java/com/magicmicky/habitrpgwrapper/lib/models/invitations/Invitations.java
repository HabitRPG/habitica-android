package com.magicmicky.habitrpgwrapper.lib.models.invitations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keithholliday on 7/2/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Invitations extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    public String user_id;

    @SerializedName("party")
    @Expose
    private PartyInvite party;

    @SerializedName("guilds")
    @Expose
    private List<GuildInvite> guilds = new ArrayList<GuildInvite>();

    /**
     * @return The party invite
     */
    public PartyInvite getParty() {
        return party;
    }

    /**
     * @param party The party
     */
    public void setParty(PartyInvite party) {
        this.party = party;
    }

    /**
     * @return The guilds invite
     */
    public List<GuildInvite> getGuilds() {
        return guilds;
    }

    /**
     * @param guilds The guilds
     */
    public void setGuilds(List<GuildInvite> guilds) {
        this.guilds = guilds;
    }
}
