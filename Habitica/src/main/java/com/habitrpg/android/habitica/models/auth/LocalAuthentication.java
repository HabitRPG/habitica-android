package com.habitrpg.android.habitica.models.auth;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by admin on 18/11/15.
 */
public class LocalAuthentication extends RealmObject {

    @PrimaryKey
    public String userID;
    String username;
    String email;

    public String getEmail() {
        return getEmail();
    }

    public void setEmail(String email) {
        this.setEmail(email);
    }

    public String getUsername() {
        return getUsername();
    }

    public void setUsername(String username) {
        this.setUsername(username);
    }
}
