package com.habitrpg.android.habitica.models.auth;

/**
 * Created by magicmicky on 04/02/15.
 */
public class UserAuthResponse {
    //we need apiToken and token, as both are possible returns
    private String apiToken;
    private String token;
    private Boolean newUser = false;
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

    public Boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }
}
