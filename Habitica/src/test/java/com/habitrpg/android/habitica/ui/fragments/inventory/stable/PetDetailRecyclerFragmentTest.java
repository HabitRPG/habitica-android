package com.habitrpg.android.habitica.ui.fragments.inventory.stable;

import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.habitrpg.android.habitica.models.user.Items;

import java.util.HashMap;

public class PetDetailRecyclerFragmentTest extends BaseFragmentTests<PetDetailRecyclerFragment> {

    @Override
    public void setUp() {
        super.setUp();
        this.fragment = new PetDetailRecyclerFragment();
        User user = new User();
        user.setItems(new Items());
        user.getItems().setPets(new HashMap<>());
        this.fragment.setUser(user);
    }
}
