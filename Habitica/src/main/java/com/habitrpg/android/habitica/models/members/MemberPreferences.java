package com.habitrpg.android.habitica.models.members;


import com.habitrpg.android.habitica.models.AvatarPreferences;
import com.habitrpg.android.habitica.models.user.Hair;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MemberPreferences extends RealmObject implements AvatarPreferences {

    @PrimaryKey
    private String userId;

    public Hair hair;
    private boolean costume, disableClasses, sleep;
    private String shirt, skin, size, background, chair;

    public MemberPreferences() {
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (hair != null && !hair.isManaged()) {
            hair.setUserId(userId);
        }
    }

    public String getUserId() {
        return userId;
    }

    public Hair getHair() {
        return hair;
    }

    public void setHair(Hair hair) {
        this.hair = hair;
    }

    public boolean getCostume() {
        return costume;
    }

    public void setCostume(boolean costume) {
        this.costume = costume;
    }

    public boolean getDisableClasses() {
        return disableClasses;
    }

    public void setDisableClasses(boolean disableClasses) {
        this.disableClasses = disableClasses;
    }

    public boolean getSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public String getShirt() {
        return shirt;
    }

    public void setShirt(String shirt) {
        this.shirt = shirt;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getChair() {
        return chair;
    }

    public void setChair(String chair) {
        this.chair = chair;
    }
}
