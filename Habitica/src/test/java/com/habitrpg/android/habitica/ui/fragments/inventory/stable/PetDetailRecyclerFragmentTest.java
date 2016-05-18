package com.habitrpg.android.habitica.ui.fragments.inventory.stable;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;

import java.util.HashMap;

public class PetDetailRecyclerFragmentTest extends BaseFragmentTests<PetDetailRecyclerFragment> {

    @Override
    public void setUp() {
        super.setUp();
        this.fragment = new PetDetailRecyclerFragment();
        HabitRPGUser user = new HabitRPGUser();
        user.setItems(new Items());
        user.getItems().setPets(new HashMap<>());
        this.fragment.setUser(user);
    }
}
