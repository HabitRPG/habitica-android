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

        loadTaskLists();
        return v;
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                ChallengeTaskRecyclerViewFragment fragment;

                switch (position) {
                    case 0:
                        fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_HABIT, observableHabitList);
                        break;
                    case 1:
                        fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_DAILY, observableDailyList);
                        break;
                    case 3:
                        fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_REWARD, observableRewardList);
                        break;
                    default:
                        fragment = ChallengeTaskRecyclerViewFragment.newInstance(user, Task.TYPE_TODO, observableTodoList);
                }

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

        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }

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
                }, throwable -> {
                });
    }
}
