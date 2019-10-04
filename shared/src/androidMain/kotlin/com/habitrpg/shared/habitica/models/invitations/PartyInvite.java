package com.habitrpg.shared.habitica.models.invitations;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PartyInvite extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private String inviter;

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
}
