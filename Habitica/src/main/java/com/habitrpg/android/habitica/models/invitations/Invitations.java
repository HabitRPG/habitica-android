package com.habitrpg.android.habitica.models.invitations;

import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Invitations extends RealmObject {

    @PrimaryKey
    private String userId;

    User user;
    private PartyInvite party;
    private RealmList<GuildInvite> guilds;

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
    public void setGuilds(RealmList<GuildInvite> guilds) {
        this.guilds = guilds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
