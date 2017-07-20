package com.habitrpg.android.habitica.models;


import com.habitrpg.android.habitica.models.user.Flags;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.ui.AvatarView;

import java.util.EnumMap;

/**
 * Created by phillip on 29.06.17.
 */

public interface Avatar {
    EnumMap<AvatarView.LayerType,String> getAvatarLayerMap();

    String getCurrentMount();

    String getCurrentPet();

    String getBackground();

    boolean getSleep();

    Stats getStats();

    Preferences getPreferences();

    Flags getFlags();

    Integer getGemCount();

    Integer getHourglassCount();
}
