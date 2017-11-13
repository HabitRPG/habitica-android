package com.habitrpg.android.habitica.models;


import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Stats;

/**
 * Created by phillip on 29.06.17.
 */

public interface Avatar {
    String getCurrentMount();

    String getCurrentPet();

    String getBackground();

    boolean getSleep();

    Stats getStats();

    AvatarPreferences getPreferences();

    Integer getGemCount();

    Integer getHourglassCount();

    @Nullable
    Outfit getCostume();
    @Nullable
    Outfit getEquipped();

    boolean hasClass();

    boolean isValid();
}
