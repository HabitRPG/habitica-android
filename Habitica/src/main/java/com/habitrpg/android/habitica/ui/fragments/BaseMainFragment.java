package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.roughike.bottombar.BottomBar;

import javax.inject.Inject;

public abstract class BaseMainFragment extends BaseFragment {

    @Inject
    public ApiClient apiClient;
    @Inject
    protected
    UserRepository userRepository;
    @Nullable
    public MainActivity activity;
    @Nullable
    public TabLayout tabLayout;
    @Nullable
    private CollapsingToolbarLayout collapsingToolbar;
    @Nullable
    protected FrameLayout toolbarAccessoryContainer;
    @Nullable
    public BottomBar bottomNavigation;
    public ViewGroup floatingMenuWrapper;
    public boolean usesTabLayout;
    public boolean usesBottomNavigation = false;
    public int fragmentSidebarPosition;
    @Inject
    protected SoundManager soundManager;
    @Nullable
    protected User user;

    public void setUser(@Nullable User user) {
        this.user = user;
    }

    public void updateUserData(User user) {
        this.user = user;
    }

    public void setTabLayout(@Nullable TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    public void setBottomNavigation(@Nullable BottomBar bottomNavigation) {
        this.bottomNavigation = bottomNavigation;
    }

    public void setFloatingMenuWrapper(ViewGroup view) {
        this.floatingMenuWrapper = view;
    }

    public void setActivity(@Nullable MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity().getClass().equals(MainActivity.class)) {
            this.activity = (MainActivity) getActivity();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("userId")) {
            String userId = savedInstanceState.getString("userId");
            if (userId != null && userRepository != null) {
                compositeSubscription.add(userRepository.getUser(userId).subscribe(habitRPGUser -> user = habitRPGUser, RxErrorHandler.handleEmptyError()));
            }
        }

        if (bottomNavigation != null) {
            if (this.usesBottomNavigation) {
                bottomNavigation.removeOnTabSelectListener();
                bottomNavigation.removeOnTabReselectListener();
                bottomNavigation.setVisibility(View.VISIBLE);
            } else {
                bottomNavigation.setVisibility(View.GONE);
            }
        }

        if (floatingMenuWrapper != null) {
            floatingMenuWrapper.removeAllViews();
        }

        setHasOptionsMenu(true);

        if (activity != null) {
            activity.setActiveFragment(this);
        }

        updateTabLayoutVisibility();

        return null;
    }

    private void updateTabLayoutVisibility() {
        if (tabLayout != null) {
            if (this.usesTabLayout) {
                tabLayout.removeAllTabs();
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (userRepository != null) {
            userRepository.close();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (user != null) {
            outState.putString("userId", user.getId());
        }

        super.onSaveInstanceState(outState);
    }

    public String customTitle() {
        return "";
    }

    public void hideToolbar() {
        activity.avatar_with_bars.setVisibility(View.GONE);
    }

    public void showToolbar() {
        activity.avatar_with_bars.setVisibility(View.VISIBLE);
    }

    public void disableToolbarScrolling() {
        if (collapsingToolbar != null) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbar.getLayoutParams();
            params.setScrollFlags(0);
        }
    }

    public  void enableToolbarScrolling() {
        if (collapsingToolbar != null) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbar.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        }
    }

    public void setCollapsingToolbar(@Nullable CollapsingToolbarLayout collapsingToolbar) {
        this.collapsingToolbar = collapsingToolbar;
    }

    public void setToolbarAccessoryContainer(FrameLayout toolbarAccessoryContainer) {
        this.toolbarAccessoryContainer = toolbarAccessoryContainer;
    }
}
