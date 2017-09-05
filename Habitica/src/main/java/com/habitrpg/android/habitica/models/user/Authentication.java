package com.habitrpg.android.habitica.models.user;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.auth.LocalAuthentication;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Authentication extends RealmObject {

    @PrimaryKey
    private String userId;

    @SerializedName("local")
    public LocalAuthentication localAuthentication;
    User user;

    public LocalAuthentication getLocalAuthentication() {
        return localAuthentication;
    }

    public void setLocalAuthentication(LocalAuthentication LocalAuthentication) {
        this.localAuthentication = LocalAuthentication;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
