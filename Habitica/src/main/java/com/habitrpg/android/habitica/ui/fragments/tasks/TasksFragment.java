package com.habitrpg.android.habitica.ui.fragments.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.events.commands.RefreshUserCommand;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder;
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.views.tasks.TaskFilterDialog;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

public class TasksFragment extends BaseMainFragment {

    private static final int TASK_CREATED_RESULT = 1;
    private static final int TASK_UPDATED_RESULT = 2;

    public ViewPager viewPager;
    @Inject
    public TaskFilterHelper taskFilterHelper; // This will be used for this fragment. Currently being used to help filtering
    MenuItem refreshItem;
    FloatingActionMenu floatingMenu;
    SparseArray<TaskRecyclerViewFragment> viewFragmentsDictionary = new SparseArray<>();

    private boolean displayingTaskForm;
    private TextView filterCountTextView;

    public void setActivity(MainActivity activity) {
        super.setActivity(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.usesTabLayout = false;
        this.usesBottomNavigation = true;
        this.displayingTaskForm = false;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);


        viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        View view = inflater.inflate(R.layout.floating_menu_tasks, floatingMenuWrapper, true);
        if (view.getClass() == FloatingActionMenu.class) {
            floatingMenu = (FloatingActionMenu) view;
        } else {
            ViewGroup frame = (ViewGroup) view;
            floatingMenu = (FloatingActionMenu) frame.findViewById(R.id.fab_menu);
        }
        FloatingActionButton habit_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_habit);
        habit_fab.setOnClickListener(v1 -> openNewTaskActivity("habit"));
        FloatingActionButton daily_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_daily);
        daily_fab.setOnClickListener(v1 -> openNewTaskActivity("daily"));
        FloatingActionButton todo_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_todo);
        todo_fab.setOnClickListener(v1 -> openNewTaskActivity("todo"));
        FloatingActionButton reward_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_reward);
        reward_fab.setOnClickListener(v1 -> openNewTaskActivity("reward"));
        floatingMenu.setOnMenuButtonLongClickListener(this::onFloatingMenuLongClicked);

        this.activity.unlockDrawer(GravityCompat.END);

        loadTaskLists();

        bottomNavigation.setOnTabSelectListener(tabId -> {
            if (tabId == R.id.tab_habits) {
                viewPager.setCurrentItem(0);
            } else if (tabId == R.id.tab_dailies) {
                viewPager.setCurrentItem(1);
            } else if (tabId == R.id.tab_todos) {
                viewPager.setCurrentItem(2);
            } else if (tabId == R.id.tab_rewards) {
                viewPager.setCurrentItem(3);
            }
        });

        return v;
    }

    private boolean onFloatingMenuLongClicked(View view) {
        int currentType = viewPager.getCurrentItem();
        TaskRecyclerViewFragment currentFragment = viewFragmentsDictionary.get(currentType);
        String className = currentFragment.getClassName();
        openNewTaskActivity(className);
        return true;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_activity, menu);

        RelativeLayout badgeLayout = (RelativeLayout) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        filterCountTextView = (TextView) badgeLayout.findViewById(R.id.badge_textview);
        badgeLayout.setOnClickListener(view -> showFilterDialog());
        updateFilterIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                showFilterDialog();
                return true;
            case R.id.action_reload:
                refreshItem = item;
                refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        TaskFilterDialog dialog = new TaskFilterDialog(getContext(), HabiticaBaseApplication.getComponent());
        if (user != null) {
            dialog.setTags(user.getTags());
        }
        dialog.setActiveTags(taskFilterHelper.getTags());
        String taskType = getActiveFragment().classType;
        dialog.setTaskType(taskType, taskFilterHelper.getActiveFilter(taskType));
        dialog.setListener((activeTaskFilter, activeTags) -> {
            int activePos = viewPager.getCurrentItem();
            if (activePos >= 1) {
                viewFragmentsDictionary.get(activePos-1).recyclerAdapter.filter();
            }
            getActiveFragment().setActiveFilter(activeTaskFilter);
            if (activePos < viewPager.getAdapter().getCount()) {
                viewFragmentsDictionary.get(activePos+1).recyclerAdapter.filter();
            }
            taskFilterHelper.setTags(activeTags);
            updateFilterIcon();
        });
        dialog.show();
    }

    public void refresh() {
        getActiveFragment().onRefresh();
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                TaskRecyclerViewFragment fragment;
                SortableTasksRecyclerViewAdapter.SortTasksCallback sortCallback =
                        (task, from, to) -> {
                            if (apiClient != null){
                                apiClient.postTaskNewPosition(task.getId(), String.valueOf(to))
                                        .subscribe(aVoid -> {
                                        }, e -> {
                                        });
                            }
                        };

                switch (position) {
                    case 0:
                        fragment = TaskRecyclerViewFragment.newInstance(user, Task.TYPE_HABIT, sortCallback);
                        break;
                    case 1:
                        fragment = TaskRecyclerViewFragment.newInstance(user, Task.TYPE_DAILY, sortCallback);
                        break;
                    case 3:
                        fragment = TaskRecyclerViewFragment.newInstance(user, Task.TYPE_REWARD, null);
                        break;
                    default:
                        fragment = TaskRecyclerViewFragment.newInstance(user, Task.TYPE_TODO,sortCallback);
                }

                viewFragmentsDictionary.put(position, fragment);

                return fragment;
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return activity.getString(R.string.habits);
                    case 1:
                        return activity.getString(R.string.dailies);
                    case 2:
                        return activity.getString(R.string.todos);
                    case 3:
                        return activity.getString(R.string.rewards);
                }
                return "";
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.selectTabAtPosition(position);
                updateFilterIcon();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateFilterIcon() {
        int filterCount = 0;
        if (getActiveFragment() != null) {
            filterCount = taskFilterHelper.howMany(getActiveFragment().classType);
        }
        if (filterCount == 0) {
            filterCountTextView.setText(null);
            filterCountTextView.setVisibility(View.GONE);
        } else {
            filterCountTextView.setText(String.valueOf(filterCount));
            filterCountTextView.setVisibility(View.VISIBLE);
        }

    }
    // endregion

    //region Events
    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        if (this.user != null) {
            for (int index = 0; index < viewFragmentsDictionary.size(); index++) {
                TaskRecyclerViewFragment fragment = viewFragmentsDictionary.get(index);
                if (fragment != null) {
                    BaseTasksRecyclerViewAdapter adapter = fragment.recyclerAdapter;
                    if (adapter.getClass().equals(DailiesRecyclerViewHolder.class)) {
                        final DailiesRecyclerViewHolder dailyAdapter = (DailiesRecyclerViewHolder) fragment.recyclerAdapter;
                        dailyAdapter.dailyResetOffset = this.user.getPreferences().getDayStart();
                    }
                    AsyncTask.execute(() -> adapter.loadContent(true));
                }
            }
        }
    }

    private void openNewTaskActivity(String type) {
        if (this.displayingTaskForm) {
            return;
        }

        String allocationMode = "";
        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null) {
            allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();
        }

        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type);
        bundle.putString(TaskFormActivity.USER_ID_KEY, this.user != null ? this.user.getId() : null);
        bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);

        Intent intent = new Intent(activity, TaskFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (this.isAdded()) {
            this.displayingTaskForm = true;
            startActivityForResult(intent, TASK_CREATED_RESULT);
        }
    }

    @Subscribe
    public void onEvent(RefreshUserCommand event) {
        getActiveFragment().onRefresh();
    }

    @Nullable
    private TaskRecyclerViewFragment getActiveFragment() {
        return viewFragmentsDictionary.get(viewPager.getCurrentItem());
    }

    @Subscribe
    public void onEvent(TaskTappedEvent event) {
        if (this.displayingTaskForm) {
            return;
        }

        String allocationMode = "";
        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null) {
            allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();
        }

        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, event.Task.getType());
        bundle.putString(TaskFormActivity.TASK_ID_KEY, event.Task.getId());
        bundle.putString(TaskFormActivity.USER_ID_KEY, this.user != null ? this.user.getId() : null);
        bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);

        Intent intent = new Intent(activity, TaskFormActivity.class);
        intent.putExtras(bundle);
        this.displayingTaskForm = true;
        if (isAdded()) {
            startActivityForResult(intent, TASK_UPDATED_RESULT);
        }
    }

    @Subscribe
    public void onEvent(AddNewTaskCommand event) {
        openNewTaskActivity(event.ClassType.toLowerCase(Locale.US));
    }

    @Subscribe
    public void onEvent(final TaskSaveEvent event) {
        floatingMenu.close(true);
    }

    //endregion Events

    @Override
    public void onDestroyView() {
        this.activity.lockDrawer(GravityCompat.END);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (TASK_CREATED_RESULT):
                this.displayingTaskForm = false;
                onTaskCreatedResult(resultCode, data);
                break;
            case (TASK_UPDATED_RESULT):
                this.displayingTaskForm = false;
                break;
        }
    }

    private void onTaskCreatedResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String taskType = data.getStringExtra(TaskFormActivity.TASK_TYPE_KEY);
            switchToTaskTab(taskType);
        }
    }

    private void switchToTaskTab(String taskType) {
        for (int index = 0; index < viewFragmentsDictionary.size(); index++) {
            if (viewFragmentsDictionary.get(index).getClassName().equals(taskType)) {
                viewPager.setCurrentItem(index);
            }
        }
    }

    @Nullable
    @Override
    public String getDisplayedClassName() {
        return null;
    }

    @Nullable
    @Override
    public String customTitle() {
        return null;
    }
}