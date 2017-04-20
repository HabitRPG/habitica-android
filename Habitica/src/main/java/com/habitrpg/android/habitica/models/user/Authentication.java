package com.habitrpg.android.habitica.models.user;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.auth.LocalAuthentication;

import io.realm.RealmObject;

public class Authentication extends RealmObject {

    @SerializedName("local")
    public LocalAuthentication localAuthentication;
    User user;

    public LocalAuthentication getLocalAuthentication() {
        return localAuthentication;
    }

    public void setLocalAuthentication(LocalAuthentication LocalAuthentication) {
        this.localAuthentication = LocalAuthentication;
    }
}
