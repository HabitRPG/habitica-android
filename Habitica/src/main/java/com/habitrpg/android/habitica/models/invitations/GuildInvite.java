package com.habitrpg.android.habitica.models.invitations;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GuildInvite extends RealmObject {

    Invitations invitations;
    private String inviter;
    private String name;
    @PrimaryKey
    private String id;

    private Boolean publicGuild;

    /**
     * @return The inviter
     */
    public String getInviter() {
        return inviter;
    }

    /**
     * @param inviter The inviter
     */
    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    public Boolean getPublicGuild() {
        return publicGuild;
    }

    public void setPublicGuild(Boolean publicGuild) {
        this.publicGuild = publicGuild;
    }
}
