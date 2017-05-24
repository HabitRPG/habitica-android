package com.habitrpg.android.habitica.ui.fragments.inventory.stable;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Items;

import java.util.HashMap;

import io.realm.RealmList;

public class MountDetailRecyclerFragmentTest extends BaseFragmentTests<MountDetailRecyclerFragment> {

    @Override
    public void setUp() {
        super.setUp();
        this.fragment = new MountDetailRecyclerFragment();
        User user = new User();
        user.setItems(new Items());
        user.getItems().setMounts(new RealmList<>());
        this.fragment.setUser(user);
    }
}
