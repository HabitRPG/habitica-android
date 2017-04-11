package com.habitrpg.android.habitica.data;


import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.SetupCustomization;

import java.util.List;

public interface SetupCustomizationRepository {

    public List<SetupCustomization> getCustomizations(String type, HabitRPGUser user);
    public List<SetupCustomization> getCustomizations(String type, String subtype, HabitRPGUser user);
}
