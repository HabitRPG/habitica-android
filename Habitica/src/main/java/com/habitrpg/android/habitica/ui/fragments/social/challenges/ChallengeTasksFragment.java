package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;
import java.util.Map;

public class ChallengeTasksFragment extends BaseMainFragment {

    public ViewPager viewPager;
    private String challengeId;

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    ObservableList<Task> observableTodoList = new ObservableArrayList<>();
    ObservableList<Task> observableDailyList = new ObservableArrayList<>();
    ObservableList<Task> observableHabitList = new ObservableArrayList<>();
    ObservableList<Task> observableRewardList = new ObservableArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);


        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        apiHelper.apiService.getChallengeTasks(challengeId)
                .compose(this.apiHelper.configureApiCallObserver())
                .subscribe(taskList -> {
                    ArrayList<Task> todos = new ArrayList<>();
                    ArrayList<Task> habits = new ArrayList<>();
                    ArrayList<Task> dailies = new ArrayList<>();
                    ArrayList<Task> rewards = new ArrayList<>();

                    for (Map.Entry<String, Task> entry : taskList.tasks.entrySet()) {
                        switch (entry.getValue().type) {
                            case Task.TYPE_TODO:
                                todos.add(entry.getValue());
                                break;
                            case Task.TYPE_HABIT:

                                habits.add(entry.getValue());
                                break;
                            case Task.TYPE_DAILY:

                                dailies.add(entry.getValue());
                                break;
                            case Task.TYPE_REWARD:

                                rewards.add(entry.getValue());
                                break;
                        }
                    }

                    observableTodoList.addAll(todos);
                    observableDailyList.addAll(dailies);
                    observableHabitList.addAll(habits);
                    observableRewardList.addAll(rewards);

                    loadTaskLists();
                }, throwable -> {
                });

        return v;
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        int tabCount = 0;
        int habitPosition = -1;
        int dailyPosition = -1;
        int todoPosition = -1;
        int rewardPosition = -1;

        if (observableHabitList.size() > 0) {
            habitPosition = tabCount++;
        }

        if (observableDailyList.size() > 0) {
            dailyPosition = tabCount++;
        }

        if (observableTodoList.size() > 0) {
            todoPosition = tabCount++;
        }

        if (observableRewardList.size() > 0) {
            rewardPosition = tabCount++;
        }

        final int finalTabCount = tabCount;
        final int finalHabitPosition = habitPosition;
        final int finalDailyPosition = dailyPosition;
        final int finalTodoPosition = todoPosition;
        final int finalRewardPosition = rewardPosition;

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                ChallengeTaskRecyclerViewFragment fragment;

                if (position == finalHabitPosition) {
                    fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_HABIT, observableHabitList);
                } else if (position == finalDailyPosition) {
                    fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_DAILY, observableDailyList);
                } else if (position == finalTodoPosition) {
                    fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_TODO, observableTodoList);
                } else if (position == finalRewardPosition) {
                    fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_REWARD, observableRewardList);
                } else {
                    fragment = null;
                }

                return fragment;
            }

            @Override
            public int getCount() {
                return finalTabCount;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (position == finalHabitPosition) {
                    return getContext().getString(R.string.habits);
                } else if (position == finalDailyPosition) {
                    return getContext().getString(R.string.dailies);
                } else if (position == finalTodoPosition) {
                    return getContext().getString(R.string.todos);
                } else if (position == finalRewardPosition) {
                    return getContext().getString(R.string.rewards);
                }

                return "";
            }
        });

        if (tabLayout != null) {

            if (finalTabCount != 1) {
                tabLayout.setupWithViewPager(viewPager);
            } else {
                tabLayout.setVisibility(View.GONE);
            }
        }
    }
}
