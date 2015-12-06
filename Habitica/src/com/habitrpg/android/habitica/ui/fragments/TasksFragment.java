package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.MainActivity;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.TaskFormActivity;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskCreationCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.TaskUpdateCallback;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskLongPressedEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.events.commands.FilterTasksByTagsCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel;
import com.habitrpg.android.habitica.ui.EditTextDrawer;
import com.habitrpg.android.habitica.ui.adapter.HabitItemRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.IReceiveNewEntries;
import com.habitrpg.android.habitica.ui.helpers.Debounce;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.SuppressedModals;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TasksFragment extends BaseFragment implements TaskScoringCallback.OnTaskScored, OnCheckedChangeListener {

    static final int TASK_CREATED_RESULT = 1;
    static final int TASK_UPDATED_RESULT = 2;

    public ViewPager viewPager;
    Drawer filterDrawer;

    MenuItem refreshItem;

    private MaterialDialog faintDialog;

    FloatingActionMenu floatingMenu;

    Map<Integer, TaskRecyclerViewFragment> ViewFragmentsDictionary = new HashMap<>();

    private TagsHelper tagsHelper;
    private ContentCache contentCache;

    public void setActivity(MainActivity activity) {
        super.setActivity(activity);
        contentCache = new ContentCache(mAPIHelper.apiService);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_tasks, container, false);


        viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        View view = inflater.inflate(R.layout.floating_menu_tasks, floatingMenuWrapper, true);
        if (view.getClass() == FrameLayout.class) {
            FrameLayout frame = (FrameLayout) view;
            floatingMenu = (FloatingActionMenu) frame.findViewById(R.id.fab_menu);
        } else {
            floatingMenu = (FloatingActionMenu) view;
        }
        FloatingActionButton habit_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_habit);
        habit_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewTaskActivity("habit");
            }
        });
        FloatingActionButton daily_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_daily);
        daily_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewTaskActivity("daily");
            }
        });
        FloatingActionButton todo_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_todo);
        todo_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewTaskActivity("todo");
            }
        });
        FloatingActionButton reward_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_reward);
        reward_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewTaskActivity("reward");
            }
        });

        filterDrawer = new DrawerBuilder()
                .withActivity(activity)
                .withDrawerGravity(Gravity.RIGHT)
                .withCloseOnClick(false)
                .append(activity.drawer);

        filterDrawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);

        viewPager.setCurrentItem(0);
        this.tagsHelper = new TagsHelper();

        loadTaskLists();

        if (user != null) {
            fillTagFilterDrawer(user.getTags());
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                filterDrawer.openDrawer();
                return true;
            case R.id.action_reload:
                refreshItem = item;
                refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
     /* Attach a rotating ImageView to the refresh item as an ActionView */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_actionview, null);

        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.clockwise_rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);

        mAPIHelper.retrieveUser(new HabitRPGUserCallback(activity));
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

        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        if (refreshItem != null) {
            View actionView = refreshItem.getActionView();
            if (actionView != null) {
                actionView.clearAnimation();
            }
            refreshItem.setActionView(null);
        }
        if (this.user != null) {
            fillTagFilterDrawer(user.getTags());
            TaskRecyclerViewFragment fragment = ViewFragmentsDictionary.get(2);
            if (fragment != null) {
                HabitItemRecyclerViewAdapter adapter = (HabitItemRecyclerViewAdapter) fragment.mAdapter;
                adapter.dailyResetOffset = this.user.getPreferences().getDayStart();
            }
            for (TaskRecyclerViewFragment fragm : ViewFragmentsDictionary.values()) {
                if (fragm != null) {
                    final HabitItemRecyclerViewAdapter adapter = (HabitItemRecyclerViewAdapter) fragm.mAdapter;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            adapter.loadContent(true);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onTaskDataReceived(TaskDirectionData data) {
        notifyUser(data.getExp(), data.getHp(), data.getGp(), data.getLvl(), data.getDelta());
        if (data.get_tmp() != null) {
            if (data.get_tmp().getDrop() != null) {
                activity.showSnackbar(data.get_tmp().getDrop().getDialog(), MainActivity.SnackbarDisplayType.DROP);
            }
        }
    }

    @Override
    public void onTaskScoringFailed() {

    }


    private void openNewTaskActivity(String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putStringArrayList("tagsId", new ArrayList<String>(this.tagsHelper.getTags()));

        Intent intent = new Intent(activity, TaskFormActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivityForResult(intent, TASK_CREATED_RESULT);
    }

    // endregion



    //region Events

    public void onEvent(final CreateTagCommand event) {
        final Tag t = new Tag();
        t.setName(event.tagName);

        mAPIHelper.apiService.createTag(t, new Callback<List<Tag>>() {
            @Override
            public void success(List<Tag> tags, Response response) {
                // Since we get a list of all tags, we just save them all
                for(Tag onlineTag : tags){
                    onlineTag.user_id = user.getId();
                    onlineTag.async().save();
                }

                fillTagFilterDrawer(tags);
            }

            @Override
            public void failure(RetrofitError error) {
                activity.showSnackbar("Error: " + error.getMessage(), MainActivity.SnackbarDisplayType.FAILURE);
            }
        });
    }

    public void onEvent(TaskTappedEvent event) {
        Bundle bundle = new Bundle();
        bundle.putString("type", event.Task.getType());
        bundle.putString("taskId", event.Task.getId());
        bundle.putStringArrayList("tagsId", new ArrayList<String>(this.tagsHelper.getTags()));
        Intent intent = new Intent(activity, TaskFormActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, TASK_UPDATED_RESULT);
    }

    public void onEvent(TaskLongPressedEvent event) {
    }

    public void onEvent(TaskCheckedCommand event) {
        mAPIHelper.updateTaskDirection(event.Task.getId(), event.Task.getCompleted() ? TaskDirection.down : TaskDirection.up, new TaskScoringCallback(this, event.Task.getId()));
    }

    public void onEvent(HabitScoreEvent event) {
        mAPIHelper.updateTaskDirection(event.Habit.getId(), event.Up ? TaskDirection.up : TaskDirection.down, new TaskScoringCallback(this, event.Habit.getId()));
    }

    public void onEvent(AddNewTaskCommand event) {
        openNewTaskActivity(event.ClassType.toLowerCase());
    }

    public void onEvent(final BuyRewardCommand event) {
        final String rewardKey = event.Reward.getId();

        if (user.getStats().getGp() < event.Reward.getValue()) {
            activity.showSnackbar("Not enough Gold", MainActivity.SnackbarDisplayType.FAILURE);
            return;
        }

        if (event.Reward.specialTag == "item") {
            if (rewardKey.equals("potion")) {
                int currentHp = user.getStats().getHp().intValue();
                int maxHp = user.getStats().getMaxHealth();

                if (currentHp == maxHp) {
                    activity.showSnackbar("You don't need to buy an health potion", MainActivity.SnackbarDisplayType.FAILURE);
                    return;
                }
            }

            mAPIHelper.apiService.buyItem(event.Reward.getId(), new Callback<Void>() {

                @Override
                public void success(Void aVoid, Response response) {
                    switch (rewardKey) {
                        case "potion":
                            double newHp = Math.min(user.getStats().getMaxHealth(), user.getStats().getHp() + 15);
                            user.getStats().setHp(newHp);


                            break;
                        default:

                            // TODO Add bought item to the avatar

                            break;
                    }

                    activity.showSnackbar("Buy Reward Successful " + event.Reward.getText());
                }

                @Override
                public void failure(RetrofitError error) {
                    activity.showSnackbar("Buy Reward Error " + event.Reward.getText(), MainActivity.SnackbarDisplayType.FAILURE);
                }
            });
        } else {
            // user created Rewards

            mAPIHelper.updateTaskDirection(rewardKey, TaskDirection.down, new TaskScoringCallback(this, rewardKey));
        }
    }

    public void onEvent(final TaskSaveEvent event) {
        Task task = (Task) event.task;
        if (event.created) {
            this.mAPIHelper.createNewTask(task, new TaskCreationCallback());
            updateTags(event.task.getTags());
        } else {
            this.mAPIHelper.updateTask(task, new TaskUpdateCallback());
        }
    }

    public void onEvent(ToggledInnStateEvent event) {
        user.getPreferences().setSleep(event.Inn);
    }

    //endregion Events

    private void notifyUser(double xp, double hp, double gold,
                            int lvl, double delta) {
        StringBuilder message = new StringBuilder();
        MainActivity.SnackbarDisplayType displayType = MainActivity.SnackbarDisplayType.NORMAL;
        if (lvl > user.getStats().getLvl()) {
            displayLevelUpDialog(lvl);

            this.mAPIHelper.retrieveUser(new HabitRPGUserCallback(activity));
            user.getStats().setLvl((int) lvl);
            activity.showSnackbar(message.toString());
        } else {
            com.magicmicky.habitrpgwrapper.lib.models.Stats stats = user.getStats();

            if (xp > stats.getExp()) {
                message.append(" + ").append(round(xp - stats.getExp(), 2)).append(" XP");
                user.getStats().setExp(xp);
            }
            if (hp != stats.getHp()) {
                displayType = MainActivity.SnackbarDisplayType.FAILURE;
                message.append(" - ").append(round(stats.getHp() - hp, 2)).append(" HP");
                user.getStats().setHp(hp);
            }
            if (gold > stats.getGp()) {
                message.append(" + ").append(round(gold - stats.getGp(), 2)).append(" GP");
                stats.setGp(gold);
            } else if (gold < stats.getGp()) {
                displayType = MainActivity.SnackbarDisplayType.FAILURE;
                message.append(" - ").append(round(stats.getGp() - gold, 2)).append(" GP");
                stats.setGp(gold);
            }
            activity.showSnackbar(message.toString(), displayType);

        }
    }

    static public Double round(Double value, int n) {
        return (Math.round(value * Math.pow(10, n))) / (Math.pow(10, n));
    }

    private void displayDeathDialogIfNeeded() {

        if (user.getStats().getHp() > 0) {
            return;
        }

        if (this.faintDialog == null) {
            this.faintDialog = new MaterialDialog.Builder(activity)
                    .title(R.string.faint_header)
                    .customView(R.layout.faint_dialog, true)
                    .positiveText(R.string.faint_button)
                    .positiveColorRes(R.color.worse_100)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            faintDialog = null;
                            mAPIHelper.reviveUser(new HabitRPGUserCallback(activity));
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            faintDialog = null;
                        }
                    })
                    .build();

            View customView = this.faintDialog.getCustomView();
            if (customView != null) {
                View hpBarView = customView.findViewById(R.id.hpBar);

                ValueBarBinding hpBar = DataBindingUtil.bind(hpBarView);
                hpBar.setPartyMembers(true);
                AvatarWithBarsViewModel.setHpBarData(hpBar, user.getStats(), activity);

                ImageView avatarView = (ImageView) customView.findViewById(R.id.avatarView);
                UserPicture userPicture = new UserPicture(user, activity, false, false);
                userPicture.setPictureOn(avatarView);
            }

            this.faintDialog.show();
        }
    }

    private void displayLevelUpDialog(int level) {
        SuppressedModals suppressedModals = user.getPreferences().getSuppressModals();
        if (suppressedModals != null) {
            if (suppressedModals.getLevelUp()) {
                return;
            }
        }

        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(R.string.levelup_header)
                .customView(R.layout.levelup_dialog, true)
                .positiveText(R.string.levelup_button)
                .positiveColorRes(R.color.brand_100)
                .build();

        View customView = dialog.getCustomView();
        if (customView != null) {
            TextView detailView = (TextView) customView.findViewById(R.id.levelupDetail);
            detailView.setText(this.getString(R.string.levelup_detail, level));

            ImageView avatarView = (ImageView) customView.findViewById(R.id.avatarView);
            UserPicture userPicture = new UserPicture(user, activity, false, false);
            userPicture.setPictureOn(avatarView);
        }

        dialog.show();
    }


    public void fillTagFilterDrawer(List<Tag> tagList) {
        filterDrawer.removeAllItems();
        filterDrawer.addItems(
                new SectionDrawerItem().withName("Filter by Tag"),
                new EditTextDrawer()
        );

        for (Tag t : tagList) {
            filterDrawer.addItem(new SwitchDrawerItem()
                            .withName(t.getName())
                            .withTag(t)
                            .withOnCheckedChangeListener(this)
            );
        }
    }

    private Debounce filterChangedHandler = new Debounce(1500, 1000) {
        @Override
        public void execute() {
            ArrayList<String> tagList = new ArrayList<>();

            for (Map.Entry<String, Boolean> f : tagFilterMap.entrySet()) {
                if (f.getValue()) {
                    tagList.add(f.getKey());
                }
            }
            tagsHelper.setTags(tagList);
            EventBus.getDefault().post(new FilterTasksByTagsCommand());
        }
    };

    private HashMap<String, Boolean> tagFilterMap = new HashMap<>();

    /*
        Updates concerned tags.
     */
    public void updateTags(List<TaskTag> tags) {
        Log.d("tags", "Updating tags");
        List<IDrawerItem> filters = filterDrawer.getDrawerItems();
        for (IDrawerItem filter : filters) {
            if (filter instanceof SwitchDrawerItem) {
                SwitchDrawerItem currentfilter = (SwitchDrawerItem) filter;
                Log.v("tags", "Tag " + currentfilter.getName());
                String tagId = ((Tag) currentfilter.getTag()).getId();
                for (TaskTag tag : tags) {
                    Tag currentTag = tag.getTag();


                    if (tagId != null && currentTag != null && tagId.equals(currentTag.getId())) {
                        currentfilter.withDescription("" + (currentTag.getTasks().size() + 1));
                        filterDrawer.updateItem(currentfilter);
                    }
                }
            }
        }

    }

    @Override
    public void onCheckedChanged(IDrawerItem iDrawerItem, CompoundButton compoundButton, boolean b) {
        Tag t = (Tag) iDrawerItem.getTag();
        if (t != null) {
            tagFilterMap.put(t.getId(), b);
            filterChangedHandler.hit();
        }
    }


    @Override
    public void onDestroyView() {
        DrawerLayout layout =  filterDrawer.getDrawerLayout();
        layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        super.onDestroyView();
    }
}
