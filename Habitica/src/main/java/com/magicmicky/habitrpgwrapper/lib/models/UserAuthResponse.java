package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by magicmicky on 04/02/15.
 */
public class UserAuthResponse {
    //we need apiToken and token, as both are possible returns
    private String apiToken;
    private String token;
    private String id;

    public String getToken() {
        if (this.token == null) {
            return this.apiToken;
        } else {
            return this.token;
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
