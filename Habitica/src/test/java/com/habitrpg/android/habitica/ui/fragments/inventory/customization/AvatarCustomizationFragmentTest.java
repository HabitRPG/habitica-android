package com.habitrpg.android.habitica.ui.fragments.inventory.customization;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Hair;
import com.magicmicky.habitrpgwrapper.lib.models.Preferences;

public class AvatarCustomizationFragmentTest extends BaseFragmentTests<AvatarCustomizationFragment> {

    @Override
    public void setUp() {
        super.setUp();
        this.fragment = new AvatarCustomizationFragment();
        HabitRPGUser user = new HabitRPGUser();
        user.setPreferences(new Preferences());
        user.getPreferences().setSkin("");
        user.getPreferences().setHair(new Hair());
        user.getPreferences().getHair().setColor("");
        this.fragment.setUser(user);
        fragment.type = "skin";
    }
}
