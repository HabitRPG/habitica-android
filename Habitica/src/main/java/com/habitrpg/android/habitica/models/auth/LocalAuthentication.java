package com.habitrpg.android.habitica.models.auth;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by admin on 18/11/15.
 */
public class LocalAuthentication extends RealmObject {

    @PrimaryKey
    String username;
    String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
