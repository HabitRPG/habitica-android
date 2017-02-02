package com.magicmicky.habitrpgwrapper.lib.models.invitations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by keithholliday on 7/2/16.
 */
public class PartyInvite {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("inviter")
    @Expose
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
