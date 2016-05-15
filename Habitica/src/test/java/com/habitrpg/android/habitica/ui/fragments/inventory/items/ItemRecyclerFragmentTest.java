package com.habitrpg.android.habitica.ui.fragments.inventory.items;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;

public class ItemRecyclerFragmentTest extends BaseFragmentTests<ItemRecyclerFragment> {

    public void setUp() {
        super.setUp();
        this.fragment = new ItemRecyclerFragment();
        this.fragment.itemType = "eggs";
        this.fragment.isHatching = false;
        this.fragment.isFeeding = false;
        this.fragment.itemTypeText = "";
    }
}
