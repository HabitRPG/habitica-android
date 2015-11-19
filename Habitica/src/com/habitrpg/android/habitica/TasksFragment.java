package com.habitrpg.android.habitica;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.adapter.HabitItemRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.IReceiveNewEntries;
import com.habitrpg.android.habitica.ui.fragments.TaskRecyclerViewFragment;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by admin on 18/11/15.
 */
public class TasksFragment extends BaseFragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;

    Drawer filterDrawer;

    Map<Integer, TaskRecyclerViewFragment> ViewFragmentsDictionary = new HashMap<>();

    private TagsHelper tagsHelper;
    private ContentCache contentCache;

    public void setActivity(MainActivity activity) {
        super.setActivity(activity);
        contentCache = new ContentCache(mAPIHelper.apiService);
    }

    @Override
    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tasks, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_pager);

        filterDrawer = new DrawerBuilder()
                .withActivity(activity)
                .withDrawerGravity(Gravity.RIGHT)
                .withCloseOnClick(false)
                .append(activity.drawer);

        viewPager.setCurrentItem(0);
        this.tagsHelper = new TagsHelper();

        loadTaskLists();

        return v;
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            int oldPosition = -1;

            @Override
            public Fragment getItem(int position) {
                int layoutOfType;
                TaskRecyclerViewFragment fragment;
                HabitItemRecyclerViewAdapter adapter;

                switch (position) {
                    case 0:
                        layoutOfType = R.layout.habit_item_card;
                        fragment = TaskRecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(Task.TYPE_HABIT, TasksFragment.this.tagsHelper, layoutOfType, HabitItemRecyclerViewAdapter.HabitViewHolder.class, activity), Task.TYPE_HABIT);

                        break;
                    case 1:
                        layoutOfType = R.layout.daily_item_card;
                        adapter = new HabitItemRecyclerViewAdapter(Task.TYPE_DAILY, TasksFragment.this.tagsHelper, layoutOfType, HabitItemRecyclerViewAdapter.DailyViewHolder.class, activity);
                        if (user != null) {
                            adapter.dailyResetOffset = user.getPreferences().getDayStart();
                        }
                        fragment = TaskRecyclerViewFragment.newInstance(adapter, Task.TYPE_DAILY);
                        break;
                    case 3:
                        layoutOfType = R.layout.reward_item_card;
                        adapter = new HabitItemRecyclerViewAdapter(Task.TYPE_REWARD, TasksFragment.this.tagsHelper,
                                layoutOfType, HabitItemRecyclerViewAdapter.RewardViewHolder.class, activity,
                                new HabitItemRecyclerViewAdapter.IAdditionalEntries() {
                                    @Override
                                    public void GetAdditionalEntries(final IReceiveNewEntries callBack) {

                                        // request buyable gear
                                        mAPIHelper.apiService.getInventoryBuyableGear(new Callback<List<ItemData>>() {
                                            @Override
                                            public void success(List<ItemData> itemDatas, Response response) {

                                                // get itemdata list
                                                ArrayList<String> itemKeys = new ArrayList<String>();
                                                for (ItemData item : itemDatas) {
                                                    itemKeys.add(item.key);
                                                }
                                                itemKeys.add("potion");

                                                contentCache.GetItemDataList(itemKeys, new ContentCache.GotContentEntryCallback<List<ItemData>>() {
                                                    @Override
                                                    public void GotObject(List<ItemData> obj) {
                                                        ArrayList<Task> buyableItems = new ArrayList<Task>();

                                                        for (ItemData item : obj) {
                                                            Task reward = new Task();
                                                            reward.text = item.text;
                                                            reward.notes = item.notes;
                                                            reward.value = item.value;
                                                            reward.setType("reward");
                                                            reward.specialTag = "item";
                                                            reward.setId(item.key);

                                                            buyableItems.add(reward);
                                                        }

                                                        callBack.GotAdditionalItems(buyableItems);

                                                    }
                                                });


                                            }

                                            @Override
                                            public void failure(RetrofitError error) {

                                            }
                                        });

                                    }
                                });


                        fragment = TaskRecyclerViewFragment.newInstance(adapter, Task.TYPE_REWARD);
                        break;
                    default:
                        layoutOfType = R.layout.todo_item_card;
                        fragment = TaskRecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(Task.TYPE_TODO, TasksFragment.this.tagsHelper, layoutOfType, HabitItemRecyclerViewAdapter.TodoViewHolder.class, activity), Task.TYPE_TODO);
                }

                ViewFragmentsDictionary.put(position, fragment);

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
                        return "Habits";
                    case 1:
                        return "Dailies";
                    case 2:
                        return "Todos";
                    case 3:
                        return "Rewards";
                }
                return "";
            }
        });


        tabLayout.setupWithViewPager(viewPager);

    }

    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        if (this.user != null) {
            TaskRecyclerViewFragment fragment = ViewFragmentsDictionary.get(2);
            if (fragment != null) {
                HabitItemRecyclerViewAdapter adapter =(HabitItemRecyclerViewAdapter)fragment.mAdapter;
                adapter.dailyResetOffset = this.user.getPreferences().getDayStart();
            }
        }
    }
}
