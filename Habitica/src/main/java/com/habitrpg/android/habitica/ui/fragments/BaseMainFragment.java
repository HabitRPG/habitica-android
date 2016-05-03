package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.PrefsActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

public abstract class BaseMainFragment extends BaseFragment {

    public MainActivity activity;
    public TabLayout tabLayout;
    public FrameLayout floatingMenuWrapper;
    public APIHelper mAPIHelper;
    protected HabitRPGUser user;
    public boolean usesTabLayout;
    public int fragmentSidebarPosition;


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
        this.activity = (MainActivity) getActivity();
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

        if (mAPIHelper == null) {
            mAPIHelper = new APIHelper(PrefsActivity.fromContext(getContext()));
        }

        if (floatingMenuWrapper != null) {
            floatingMenuWrapper.removeAllViews();
        }

        setHasOptionsMenu(true);

        activity.setActiveFragment(this);

        return null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (user != null) {
            outState.putString("userId", user.getId());
        }

        super.onSaveInstanceState(outState);
    }
}
