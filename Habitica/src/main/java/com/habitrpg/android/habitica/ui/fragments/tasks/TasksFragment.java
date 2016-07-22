package com.habitrpg.android.habitica.ui.fragments.tasks;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.TaskCreationCallback;
import com.habitrpg.android.habitica.callbacks.TaskScoringCallback;
import com.habitrpg.android.habitica.callbacks.TaskUpdateCallback;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.events.commands.FilterTasksByTagsCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder;
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.Debounce;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.menu.EditTextDrawer;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

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

import javax.inject.Inject;

import rx.Observer;
import rx.functions.Action1;

public class TasksFragment extends BaseMainFragment implements OnCheckedChangeListener {

    private static final int TASK_CREATED_RESULT = 1;
    private static final int TASK_UPDATED_RESULT = 2;

    public ViewPager viewPager;
    @Inject
    public TagsHelper tagsHelper; // This will be used for this fragment. Currently being used to help filtering
    MenuItem refreshItem;
    FloatingActionMenu floatingMenu;
    Map<Integer, TaskRecyclerViewFragment> ViewFragmentsDictionary = new HashMap<>();
    private ArrayList<String> tagNames; // Added this so other activities/fragments can get the String names, not IDs
    private ArrayList<String> tagIds; // Added this so other activities/fragments can get the IDs

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        this.activity.unlockDrawer(GravityCompat.END);

        loadTaskLists();

        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
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
                this.activity.openDrawer(GravityCompat.END);
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
                    .compose(apiHelper.configureApiCallObserver())
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
                TaskRecyclerViewFragment fragment;
                SortableTasksRecyclerViewAdapter.SortTasksCallback sortCallback =
                        new SortableTasksRecyclerViewAdapter.SortTasksCallback() {
                            @Override
                            public void onMove(Task task, int from, int to) {
                                if (apiHelper != null){
                                    apiHelper.apiService.postTaskNewPosition(task.getId(), String.valueOf(toIndex(to)))
                                            .compose(apiHelper.configureApiCallObserver())
                                            .subscribe(aVoid -> {
                                        new HabitRPGUserCallback(activity);
                                    });
                                }
                            }

                            private int toIndex(int to){
                                int toIndex = 0;
                                if (to > 0){
                                    boolean shouldBreak = false;
                                    for (toIndex = 0; toIndex<user.getTodos().size(); toIndex++){
                                        Task otherTask = user.getTodos().get(toIndex);
                                        if (!otherTask.getCompleted()){
                                            to--;
                                            if (shouldBreak) break;
                                            if (to == 0) shouldBreak = true;
                                        }
                                    }
                                }
                                return toIndex;
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
                        fragment = TaskRecyclerViewFragment.newInstance(user, Task.TYPE_TODO, sortCallback);
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
                    BaseTasksRecyclerViewAdapter adapter = fragm.recyclerAdapter;
                    if (adapter.getClass().equals(DailiesRecyclerViewHolder.class)) {
                        final DailiesRecyclerViewHolder dailyAdapter = (DailiesRecyclerViewHolder) fragm.recyclerAdapter;
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
        bundle.putString(TaskFormActivity.USER_ID_KEY, this.user.getId());
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
                    .subscribe(tag -> {
                        // Since we get a list of all tags, we just save them all
                        tag.user_id = user.getId();
                        tag.async().save();

                        addTagFilterDrawerItem(tag);
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
        if (HabiticaApplication.User != null && HabiticaApplication.User.getPreferences() != null) {
            allocationMode = HabiticaApplication.User.getPreferences().getAllocationMode();
        }

        Bundle bundle = new Bundle();
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, event.Task.getType());
        bundle.putString(TaskFormActivity.TASK_ID_KEY, event.Task.getId());
        bundle.putString(TaskFormActivity.USER_ID_KEY, this.user.getId());
        bundle.putString(TaskFormActivity.ALLOCATION_MODE_KEY, allocationMode);

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
                .subscribe(new TaskScoringCallback(activity, event.Task.getId()), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(ChecklistCheckedCommand event) {
        apiHelper.apiService.scoreChecklistItem(event.task.getId(), event.item.getId())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskUpdateCallback(), throwable -> {
                });
    }

    @Subscribe
    public void onEvent(HabitScoreEvent event) {
        apiHelper.apiService.postTaskDirection(event.habit.getId(), (event.Up ? TaskDirection.up : TaskDirection.down).toString())
                .compose(apiHelper.configureApiCallObserver())
                .subscribe(new TaskScoringCallback(activity, event.habit.getId()), throwable -> {
                });
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
                    .subscribe(new TaskCreationCallback(), throwable -> {
                    });
            floatingMenu.close(true);
        } else {
            this.apiHelper.apiService.updateTask(task.getId(), task)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(new TaskUpdateCallback(), throwable -> {
                    });
        }
    }

    @Subscribe
    public void onEvent(ToggledInnStateEvent event) {
        user.getPreferences().setSleep(event.Inn);
    }

    //endregion Events
    public void fillTagFilterDrawer(List<Tag> tagList) {
        if (this.tagsHelper != null) {
            List<IDrawerItem> items = new ArrayList<>();
            items.add(new SectionDrawerItem().withName("Filter by Tag"));
            items.add(new EditTextDrawer());
            for (Tag t : tagList) {
                items.add(new SwitchDrawerItem()
                        .withName(t.getName())
                        .withTag(t)
                        .withChecked(this.tagsHelper.isTagChecked(t.getId()))
                        .withOnCheckedChangeListener(this)
                );
            }
            this.activity.fillFilterDrawer(items);
        }
    }

    public void addTagFilterDrawerItem(Tag tag) {
        if (this.tagsHelper != null) {
            IDrawerItem item = new SwitchDrawerItem()
                    .withName(tag.getName())
                    .withTag(tag)
                    .withChecked(this.tagsHelper.isTagChecked(tag.getId()))
                    .withOnCheckedChangeListener(this);
            this.activity.addFilterDrawerItem(item);
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
        this.activity.lockDrawer(GravityCompat.END);
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
