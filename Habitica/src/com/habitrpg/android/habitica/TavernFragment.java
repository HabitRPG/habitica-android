package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.fragments.ChatListFragment;
import com.mikepenz.materialdrawer.DrawerBuilder;

/**
 * Created by viirus on 19/11/15.
 */
public class TavernFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_tavern, container, false);

        setFragment(new ChatListFragment(activity, "habitrpg", mAPIHelper, user, true));

        return v;
    }

    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.tavern_framelayout, fragment);
        fragmentTransaction.commit();
    }
}
