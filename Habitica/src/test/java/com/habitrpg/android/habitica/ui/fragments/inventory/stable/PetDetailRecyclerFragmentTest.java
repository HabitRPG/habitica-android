package com.habitrpg.android.habitica.ui.fragments.inventory.stable;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.user.Items;

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
