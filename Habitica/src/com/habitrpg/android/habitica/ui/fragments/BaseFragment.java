package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.MainActivity;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

/**
 * Created by admin on 18/11/15.
 */
public abstract class BaseFragment extends Fragment {

    public MainActivity activity;
    public TabLayout tabLayout;
    public FrameLayout floatingMenuWrapper;
    public APIHelper mAPIHelper;
    public boolean usesTabLayout;
    public int fragmentSidebarPosition;
    protected HabitRPGUser user;
    private boolean registerEventBus = false;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (tabLayout != null) {
            if (this.usesTabLayout) {
                tabLayout.setVisibility(View.VISIBLE);
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        }

        if (floatingMenuWrapper != null) {
            floatingMenuWrapper.removeAllViews();
        }

        // Receive Events
        try {
            EventBus.getDefault().register(this);
            registerEventBus = true;
        } catch (EventBusException ignored) {

        }

        setHasOptionsMenu(true);

        activity.setActiveFragment(this);

        return null;
    }

    @Override
    public void onDestroyView() {
        if (registerEventBus) {
            EventBus.getDefault().unregister(this);
        }

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


}
