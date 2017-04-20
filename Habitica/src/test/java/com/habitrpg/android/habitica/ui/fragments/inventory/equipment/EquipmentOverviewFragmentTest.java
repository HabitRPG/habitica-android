package com.habitrpg.android.habitica.ui.fragments.inventory.equipment;

import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.habitrpg.android.habitica.models.user.Gear;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Preferences;

public class EquipmentOverviewFragmentTest extends BaseFragmentTests<EquipmentOverviewFragment> {

    @Override
    public void setUp() {
        super.setUp();
        this.fragment = new EquipmentOverviewFragment();
        User user = new User();
        user.setItems(new Items());
        user.getItems().setGear(new Gear());
        user.getItems().getGear().setCostume(new Outfit());
        user.getItems().getGear().setEquipped(new Outfit());
        user.setPreferences(new Preferences());
        user.getPreferences().setCostume(true);
        this.fragment.setUser(user);
    }
}
