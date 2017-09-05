package com.habitrpg.android.habitica.models.auth;

/**
 * Created by viirus on 13/11/15.
 */
public class UserAuthSocialTokens {
    private String client_id;
    private String access_token;

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
