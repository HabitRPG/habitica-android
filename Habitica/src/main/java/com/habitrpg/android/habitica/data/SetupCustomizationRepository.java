package com.habitrpg.android.habitica.data;


import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.SetupCustomization;

import java.util.List;

public interface SetupCustomizationRepository {

    public List<SetupCustomization> getCustomizations(String type, HabitRPGUser user);
    public List<SetupCustomization> getCustomizations(String type, String subtype, HabitRPGUser user);
}
