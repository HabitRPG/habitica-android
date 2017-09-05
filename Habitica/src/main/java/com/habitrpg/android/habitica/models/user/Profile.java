package com.habitrpg.android.habitica.models.user;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Profile extends RealmObject {

    @PrimaryKey
    private String userId;

    User user;
    private String name;
    private String blurb, imageUrl;

    public Profile(String name) {
        this(name, "", "");
    }

    public Profile(String name, String blurb, String imageUrl) {
        this.name = name;
        this.blurb = blurb;
        this.imageUrl = imageUrl;
    }

    public Profile() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
