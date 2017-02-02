package com.habitrpg.android.habitica.ui.fragments.social;

import com.habitrpg.android.habitica.ui.fragments.BaseFragmentTests;

public class ChatListFragmentTest extends BaseFragmentTests<ChatListFragment> {

    public void setUp() {
        super.setUp();
        this.fragment = new ChatListFragment();
        this.fragment.isTavern = false;
    }
}
