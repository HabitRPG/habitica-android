package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.models.user.Hair;

/**
 * Created by phillip on 15.09.17.
 */

public interface AvatarPreferences {

    String getUserId();

    Hair getHair();

    boolean getCostume();

    boolean getSleep();

    String getShirt();

    String getSkin();
    String getSize();

    String getBackground();

    String getChair();

    boolean getDisableClasses();
}
