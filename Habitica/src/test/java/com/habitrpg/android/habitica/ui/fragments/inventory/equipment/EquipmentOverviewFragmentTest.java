package com.habitrpg.android.habitica.ui.fragments.inventory.equipment;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.magicmicky.habitrpgwrapper.lib.models.Gear;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.Outfit;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;

public class EquipmentOverviewFragmentTest extends BaseFragmentTests<EquipmentOverviewFragment> {

    @Override
    public void setUp() {
        super.setUp();
        this.fragment = new EquipmentOverviewFragment();
        HabitRPGUser user = new HabitRPGUser();
        user.setItems(new Items());
        user.getItems().setGear(new Gear());
        user.getItems().getGear().setCostume(new Outfit());
        user.getItems().getGear().setEquipped(new Outfit());
        user.setPreferences(new Preferences());
        user.getPreferences().setCostume(true);
        this.fragment.setUser(user);
    }
}
