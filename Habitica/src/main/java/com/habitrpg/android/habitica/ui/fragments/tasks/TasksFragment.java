package com.habitrpg.android.habitica.ui.fragments.tasks;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskCreationCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.TaskUpdateCallback;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.events.commands.FilterTasksByTagsCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.EditTextDrawer;
import com.habitrpg.android.habitica.ui.UiUtils;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.adapter.tasks.HabitItemRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.Debounce;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TasksFragment extends BaseMainFragment implements OnCheckedChangeListener {

    private static final int TASK_CREATED_RESULT = 1;
    private static final int TASK_UPDATED_RESULT = 2;

    public ViewPager viewPager;

    MenuItem refreshItem;


    FloatingActionMenu floatingMenu;

    Map<Integer, TaskRecyclerViewFragment> ViewFragmentsDictionary = new HashMap<>();

    private TagsHelper tagsHelper; // This will be used for this fragment. Currently being used to help filtering
    private ArrayList<String> tagNames; // Added this so other activities/fragments can get the String names, not IDs
    private ArrayList<String> tagIds; // Added this so other activities/fragments can get the IDs
    private ContentCache contentCache;

    private boolean displayingTaskForm;
    private HashMap<String, Boolean> tagFilterMap = new HashMap<>();
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

    public void setActivity(MainActivity activity) {
        super.setActivity(activity);
        contentCache = new ContentCache(apiHelper.apiService);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.tagsHelper == null) {
            this.tagsHelper = new TagsHelper();
        }

        if (user != null) {
            fillTagFilterDrawer(user.getTags());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.usesTabLayout = true;
        this.displayingTaskForm = false;
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_viewpager, container, false);


        viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        View view = inflater.inflate(R.layout.floating_menu_tasks, floatingMenuWrapper, true);
        if (view.getClass() == FrameLayout.class) {
            FrameLayout frame = (FrameLayout) view;
            floatingMenu = (FloatingActionMenu) frame.findViewById(R.id.fab_menu);
        } else {
            floatingMenu = (FloatingActionMenu) view;
        }
        FloatingActionButton habit_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_habit);
        habit_fab.setOnClickListener(v1 -> openNewTaskActivity("habit"));
        FloatingActionButton daily_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_daily);
        daily_fab.setOnClickListener(v1 -> openNewTaskActivity("daily"));
        FloatingActionButton todo_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_todo);
        todo_fab.setOnClickListener(v1 -> openNewTaskActivity("todo"));
        FloatingActionButton reward_fab = (FloatingActionButton) floatingMenu.findViewById(R.id.fab_new_reward);
        reward_fab.setOnClickListener(v1 -> openNewTaskActivity("reward"));

        this.activity.filterDrawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);

        loadTaskLists();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                this.activity.filterDrawer.openDrawer();
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

        if (apiHelper != null) {
            apiHelper.retrieveUser(true)
                    .subscribe(
                            new HabitRPGUserCallback(activity),
                            throwable -> stopAnimatingRefreshItem()
                    );
        }
    }

    public void loadTaskLists() {
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();

        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                int layoutOfType;
                TaskRecyclerViewFragment fragment;
                HabitItemRecyclerViewAdapter adapter;

                switch (position) {
                    case 0:
                        layoutOfType = R.layout.habit_item_card;
                        fragment = TaskRecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(Task.TYPE_HABIT, TasksFragment.this.tagsHelper, layoutOfType, HabitItemRecyclerViewAdapter.HabitViewHolder.class, activity, 0), Task.TYPE_HABIT);

                        break;
                    case 1:
                        layoutOfType = R.layout.daily_item_card;
                        int dailyResetOffset = 0;
                        if (user != null) {
                            dailyResetOffset = user.getPreferences().getDayStart();
                        }
                        adapter = new HabitItemRecyclerViewAdapter(Task.TYPE_DAILY, TasksFragment.this.tagsHelper, layoutOfType, HabitItemRecyclerViewAdapter.DailyViewHolder.class, activity, dailyResetOffset);

                        fragment = TaskRecyclerViewFragment.newInstance(adapter, Task.TYPE_DAILY);
                        break;
                    case 3:
                        layoutOfType = R.layout.reward_item_card;
                        adapter = new HabitItemRecyclerViewAdapter(Task.TYPE_REWARD, TasksFragment.this.tagsHelper,
                                layoutOfType, HabitItemRecyclerViewAdapter.RewardViewHolder.class, activity, 0,
                                callBack -> {

                                    // request buyable gear
                                    if (apiHelper != null) {
                                        apiHelper.apiService.getInventoryBuyableGear()
                                                .compose(apiHelper.configureApiCallObserver())
                                                .subscribe(itemDatas -> {
                                            // get itemdata list
                                            ArrayList<String> itemKeys = new ArrayList<>();
                                            for (ItemData item : itemDatas) {
                                                itemKeys.add(item.key);
                                            }
                                            itemKeys.add("potion");
                                            if (user.getFlags().getArmoireEnabled())
                                                itemKeys.add("armoire");

                                            contentCache.GetItemDataList(itemKeys, obj -> {
                                                ArrayList<Task> buyableItems = new ArrayList<>();
                                                if (!isAdded()) {
                                                    return;
                                                }
                                                for (ItemData item : obj) {
                                                    Task reward = new Task();
                                                    reward.text = item.text;
                                                    reward.notes = item.notes;
                                                    reward.value = item.value;
                                                    reward.setType("reward");
                                                    reward.specialTag = "item";
                                                    reward.setId(item.key);

                                                    if (item.key.equals("armoire")) {
                                                        if (user.getFlags().getArmoireEmpty()) {
                                                            reward.notes = getResources().getString(R.string.armoireNotesEmpty);
                                                        } else {
                                                            long gearCount = new Select().count()
                                                                    .from(ItemData.class)
                                                                    .where(Condition.CombinedCondition.begin(Condition.column("klass").eq("armoire"))
                                                                            .and(Condition.column("owned").isNull())
                                                                    ).count();
                                                            reward.notes = getResources().getString(R.string.armoireNotesFull, gearCount);
                                                        }
                                                    }

                                                    buyableItems.add(reward);
                                                }

                                                callBack.GotAdditionalItems(buyableItems);
                                            });
                                        }, throwable -> {});
                                    }
                                });

                        fragment = TaskRecyclerViewFragment.newInstance(adapter, Task.TYPE_REWARD);
                        break;
                    default:
                        layoutOfType = R.layout.todo_item_card;
                        fragment = TaskRecyclerViewFragment.newInstance(new HabitItemRecyclerViewAdapter(Task.TYPE_TODO, TasksFragment.this.tagsHelper, layoutOfType, HabitItemRecyclerViewAdapter.TodoViewHolder.class, activity, 0), Task.TYPE_TODO);
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
    }

    // endregion

    //region Events

    public void updateUserData(HabitRPGUser user) {
        super.updateUserData(user);
        stopAnimatingRefreshItem();
        if (this.user != null) {
            fillTagFilterDrawer(user.getTags());

            for (TaskRecyclerViewFragment fragm : ViewFragmentsDictionary.values()) {
                if (fragm != null) {
                    final HabitItemRecyclerViewAdapter adapter = (HabitItemRecyclerViewAdapter) fragm.mAdapter;
                    adapter.dailyResetOffset = this.user.getPreferences().getDayStart();
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
        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null){
            allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();
        }

        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type);
        bundle.putStringArrayList(TaskFormActivity.TAG_IDS_KEY, new ArrayList<>(this.getTagIds()));
        bundle.putStringArrayList(TaskFormActivity.TAG_NAMES_KEY, new ArrayList<>(this.getTagNames()));
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
    public void onEvent(final CreateTagCommand event) {
        UiUtils.dismissKeyboard(activity);
        final Tag t = new Tag();
        t.setName(event.tagName);
        if (apiHelper != null) {
            apiHelper.apiService.createTag(t)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(tags -> {
                        // Since we get a list of all tags, we just save them all
                        for (Tag onlineTag : tags) {
                            onlineTag.user_id = user.getId();
                            onlineTag.async().save();
                        }

                        fillTagFilterDrawer(tags);
                    }, throwable -> {
                        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), "Error: " + throwable.getMessage(), UiUtils.SnackbarDisplayType.FAILURE);
                    });
        }
    }

    @Subscribe
    public void onEvent(TaskTappedEvent event) {
        if (this.displayingTaskForm) {
            return;
        }

        String allocationMode = "";
        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null){
            allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();
        }

        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, event.Task.getType());
        bundle.putString(TaskFormActivity.TASK_ID_KEY, event.Task.getId());
        bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);
        bundle.putStringArrayList(TaskFormActivity.TAG_IDS_KEY, new ArrayList<>(this.getTagIds()));
        bundle.putStringArrayList(TaskFormActivity.TAG_NAMES_KEY, new ArrayList<>(this.getTagNames()));

        Intent intent = new Intent(activity, TaskFormActivity.class);
        intent.putExtras(bundle);
        this.displayingTaskForm = true;
        if (isAdded()) {
            startActivityForResult(intent, TASK_UPDATED_RESULT);
        }
    }

    @Subscribe
    public void onEvent(TaskCheckedCommand event) {
        apiHelper.apiService.postTaskDirection(event.Task.getId(), (event.Task.getCompleted() ? TaskDirection.down : TaskDirection.up).toString())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskScoringCallback(activity, event.Task.getId()), throwable -> {});
    }

    @Subscribe
    public void onEvent(HabitScoreEvent event) {
        apiHelper.apiService.postTaskDirection(event.Habit.getId(), (event.Up ? TaskDirection.up : TaskDirection.down).toString())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskScoringCallback(activity, event.Habit.getId()), throwable -> {});
    }

    @Subscribe
    public void onEvent(AddNewTaskCommand event) {
        openNewTaskActivity(event.ClassType.toLowerCase());
    }

    @Subscribe
    public void onEvent(final TaskSaveEvent event) {
        Task task = event.task;
        if (event.created) {
            this.apiHelper.apiService.createItem(task)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new TaskCreationCallback(), throwable -> {});
            updateTags(event.task.getTags());
            floatingMenu.close(true);
        } else {
            this.apiHelper.apiService.updateTask(task.getId(), task)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new TaskUpdateCallback(), throwable -> {});
        }
    }

    @Subscribe
    public void onEvent(ToggledInnStateEvent event) {
        user.getPreferences().setSleep(event.Inn);
    }

    //endregion Events
    public void fillTagFilterDrawer(List<Tag> tagList) {
        if (this.activity.filterDrawer != null && this.tagsHelper != null) {
            this.activity.filterDrawer.removeAllItems();
            this.activity.filterDrawer.addItems(
                    new SectionDrawerItem().withName("Filter by Tag"),
                    new EditTextDrawer()
            );
            for (Tag t : tagList) {
                this.activity.filterDrawer.addItem(new SwitchDrawerItem()
                                .withName(t.getName())
                                .withTag(t)
                                .withChecked(this.tagsHelper.isTagChecked(t.getId()))
                                .withOnCheckedChangeListener(this)
                );
            }
        }
    }

    /*
        Updates concerned tags.
     */
    public void updateTags(List<TaskTag> tags) {
        Log.d("tags", "Updating tags");
        List<IDrawerItem> filters = this.activity.filterDrawer.getDrawerItems();
        for (IDrawerItem filter : filters) {
            if (filter instanceof SwitchDrawerItem) {
                SwitchDrawerItem currentfilter = (SwitchDrawerItem) filter;
                Log.v("tags", "Tag " + currentfilter.getName());
                String tagId = ((Tag) currentfilter.getTag()).getId();
                for (TaskTag tag : tags) {
                    Tag currentTag = tag.getTag();


                    if (tagId != null && currentTag != null && tagId.equals(currentTag.getId())) {
                        //This doesn't seem to work properly. Sometimes it displays more tasks than I actually have.
                        currentfilter.withDescription("" + (currentTag.getTasks().size() + 1));
                        this.activity.filterDrawer.updateItem(currentfilter);
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
        DrawerLayout layout = this.activity.filterDrawer.getDrawerLayout();
        layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (TASK_CREATED_RESULT):
            case (TASK_UPDATED_RESULT):
                this.displayingTaskForm = false;
                break;
        }
    }

    @Override
    public String getDisplayedClassName() {
        return null;
    }

    private ArrayList<String>getTagNames() {
        if (this.tagNames == null) {
            this.tagNames = new ArrayList<>();
        }
        if (this.user != null && this.user.getTags().size() != this.tagNames.size()) {
            this.tagNames.clear();
            for (Tag tag : this.user.getTags()) {
                this.tagNames.add(tag.getName());
            }
        }
        return this.tagNames;
    }

    private ArrayList<String>getTagIds() {
        if (this.tagIds == null) {
            this.tagIds = new ArrayList<>();
        }
        if (this.user != null && this.user.getTags().size() != this.tagIds.size()) {
            this.tagIds.clear();
            for (Tag tag : this.user.getTags()) {
                this.tagIds.add(tag.getId());
            }
        }
        return this.tagIds;
    }

    public void stopAnimatingRefreshItem() {
        if (refreshItem != null) {
            View actionView = refreshItem.getActionView();
            if (actionView != null) {
                actionView.clearAnimation();
            }
            refreshItem.setActionView(null);
        }
    }
}
