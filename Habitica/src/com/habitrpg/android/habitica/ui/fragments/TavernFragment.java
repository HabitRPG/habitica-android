package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;

public class TavernFragment extends BaseMainFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_tavern, container, false);

        ChatListFragment fragment = new ChatListFragment();
        fragment.configure(activity, "habitrpg", mAPIHelper, user, activity, true);
        setFragment(fragment);

        this.tutorialStepIdentifier = "tavern";
        this.tutorialText = getString(R.string.tutorial_tavern);

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
