package com.habitrpg.android.habitica;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

/**
 * Created by admin on 18/11/15.
 */
public abstract class BaseFragment extends Fragment {

    public MainActivity activity;
    APIHelper mAPIHelper;
    protected HabitRPGUser user;

    public void setUser(HabitRPGUser user) { this.user = user; }
    public void updateUserData(HabitRPGUser user) { this.user = user; }
    public void setTabLayout(TabLayout tabLayout) {}
    public void setActivity(MainActivity activity) {this.activity = activity; }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) getActivity();
    }
}
