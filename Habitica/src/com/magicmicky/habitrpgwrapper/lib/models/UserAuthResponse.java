package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by magicmicky on 04/02/15.
 */
public class UserAuthResponse {
    private String token;
    private String id;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
