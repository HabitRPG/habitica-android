package com.habitrpg.android.habitica.models.auth;

/**
 * Created by viirus on 13/11/15.
 */
public class UserAuthSocial {
    private String network;
    private UserAuthSocialTokens authResponse;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public UserAuthSocialTokens getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(UserAuthSocialTokens authResponse) {
        this.authResponse = authResponse;
    }

}
