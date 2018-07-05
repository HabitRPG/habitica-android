package com.habitrpg.android.habitica.data;


import com.habitrpg.android.habitica.models.SetupCustomization;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

public interface SetupCustomizationRepository {

    List<SetupCustomization> getCustomizations(String type, User user);
    List<SetupCustomization> getCustomizations(String type, String subtype, User user);
}
