package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import javax.inject.Inject;

public abstract class BaseMainFragment extends BaseFragment {

    @Inject
    public APIHelper apiHelper;

    @Inject
    protected SoundManager soundManager;

    public MainActivity activity;
    public TabLayout tabLayout;
    public FrameLayout floatingMenuWrapper;
    public boolean usesTabLayout;
    public int fragmentSidebarPosition;
    protected HabitRPGUser user;

    public void setUser(HabitRPGUser user) {
        this.user = user;
    }

    public void updateUserData(HabitRPGUser user) {
        this.user = user;
    }

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    public void setFloatingMenuWrapper(FrameLayout view) {
        this.floatingMenuWrapper = view;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.activity = (MainActivity) getActivity();
        } catch (ClassCastException ex) {

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("userId")) {
            String userId = savedInstanceState.getString("userId");
            this.user = new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(userId)).querySingle();
        }

        if (tabLayout != null) {
            if (this.usesTabLayout) {
                tabLayout.removeAllTabs();
                tabLayout.setVisibility(View.VISIBLE);
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        }

        if (floatingMenuWrapper != null) {
            floatingMenuWrapper.removeAllViews();
        }

        setHasOptionsMenu(true);

        if (activity != null) {
            activity.setActiveFragment(this);
        }

        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (user != null) {
            outState.putString("userId", user.getId());
        }

        super.onSaveInstanceState(outState);
    }

    public String customTitle(){
        return null;
    }
}
