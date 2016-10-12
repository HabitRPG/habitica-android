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
import com.habitrpg.android.habitica.events.ToggledEditTagsEvent;
import com.habitrpg.android.habitica.events.ToggledInnStateEvent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand;
import com.habitrpg.android.habitica.events.commands.CreateTagCommand;
import com.habitrpg.android.habitica.events.commands.DeleteTagCommand;
import com.habitrpg.android.habitica.events.commands.EditTagCommand;
import com.habitrpg.android.habitica.events.commands.FilterTasksByTagsCommand;
import com.habitrpg.android.habitica.events.commands.RefreshUserCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.events.commands.UpdateTagCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder;
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment;
import com.habitrpg.android.habitica.ui.helpers.Debounce;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.habitrpg.android.habitica.ui.menu.EditTagsDrawerItem;
import com.habitrpg.android.habitica.ui.menu.EditTagsSectionDrawer;
import com.habitrpg.android.habitica.ui.menu.EditTextDrawer;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
    private boolean editingTags;
    private List<Tag> tags;
    private List<Tag> tagsCopy;
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
            tags = user.getTags();
            fillTagFilterDrawer(tags);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.usesTabLayout = true;
        this.displayingTaskForm = false;
        this.editingTags = false;
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
                        (task, from, to) -> {
                            if (apiHelper != null){
                                apiHelper.apiService.postTaskNewPosition(task.getId(), String.valueOf(to))
                                        .compose(apiHelper.configureApiCallObserver())
                                        .subscribe(aVoid -> {
                                    new HabitRPGUserCallback(activity);
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
            fillTagFilterDrawer(tags);
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

                        tags.add(tag);
                        addTagFilterDrawerItem(tag);
                    }, throwable -> {
                        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), "Error: " + throwable.getMessage(), UiUtils.SnackbarDisplayType.FAILURE);
                    });
        }
    }

    @Subscribe
    public void onEvent(final DeleteTagCommand event) {
        final Tag t = event.tag;
        if (apiHelper != null) {
            apiHelper.apiService.deleteTag(t.getId())
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(tag -> {
                        tagFilterMap.remove(t.getId());
                        filterChangedHandler.hit();
                        removeTagFilterDrawerItem(t);
                        EventBus.getDefault().post(new RefreshUserCommand());
                    }, throwable -> {
                        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), "Error: " + throwable.getMessage(), UiUtils.SnackbarDisplayType.FAILURE);
                    });
        }
    }

    @Subscribe
    public void onEvent(final EditTagCommand event) {
        showEditTagDialog(event.tag);
    }

    @Subscribe
    public void onEvent(final UpdateTagCommand event) {
        final Tag t = event.tag;
        final String uuid = event.uuid;
        if (apiHelper != null) {
            apiHelper.apiService.updateTag(uuid,t)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(tag -> {
                        UiUtils.dismissKeyboard(this.activity);
                        updateTagFilterDrawerItem(tag);
                        EventBus.getDefault().post(new RefreshUserCommand());
                    }, throwable -> {
                        UiUtils.showSnackbar(activity, activity.getFloatingMenuWrapper(), "Error: " + throwable.getMessage(), UiUtils.SnackbarDisplayType.FAILURE);
                    });
        }
    }

    @Subscribe
    public void onEvent(RefreshUserCommand event) {
        if (apiHelper != null) {
            apiHelper.retrieveUser(true)
                    .compose(apiHelper.configureApiCallObserver())
                    .subscribe(
                            new HabitRPGUserCallback(activity),
                            throwable -> stopAnimatingRefreshItem()
                    );
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
    public void onEvent(AddNewTaskCommand event) {
        openNewTaskActivity(event.ClassType.toLowerCase());
    }

    @Subscribe
    public void onEvent(final TaskSaveEvent event) {
        floatingMenu.close(true);
    }

    //endregion Events

    public void fillTagFilterDrawer(List<Tag> tagList) {
        if (this.tagsHelper != null) {
            List<IDrawerItem> items = new ArrayList<>();

            if(this.editingTags) {
                items.add(new EditTagsSectionDrawer().withEditing(this.editingTags).withName(getString(R.string.filter_drawer_edit_tags)));
                items.add(new EditTextDrawer());
                if (tagList != null) {
                    for (Tag t : tagList) {
                        items.add(new EditTagsDrawerItem()
                                .withName(t.getName())
                                .withTag(t)
                        );
                    }
                }
                if (isAdded()) {
                    this.activity.fillFilterDrawer(items);
                }
            }else {
                items.add(new EditTagsSectionDrawer().withEditing(this.editingTags).withName(getString(R.string.filter_drawer_filter_tags)));
                items.add(new EditTextDrawer());
                if (tagList != null) {
                    for (Tag t : tagList) {
                        items.add(new SwitchDrawerItem()
                                .withName(t.getName())
                                .withTag(t)
                                .withChecked(this.tagsHelper.isTagChecked(t.getId()))
                                .withOnCheckedChangeListener(this)
                        );
                    }
                }
                if (isAdded()) {
                    this.activity.fillFilterDrawer(items);
                }
            }
        }
    }

    public void addTagFilterDrawerItem(Tag tag) {
        if (this.tagsHelper != null) {
            if(this.editingTags) {
                IDrawerItem item = new EditTagsDrawerItem()
                        .withName(tag.getName())
                        .withTag(tag);
                this.activity.addFilterDrawerItem(item);
            }else {
                IDrawerItem item = new SwitchDrawerItem()
                        .withName(tag.getName())
                        .withTag(tag)
                        .withChecked(this.tagsHelper.isTagChecked(tag.getId()))
                        .withOnCheckedChangeListener(this);
                this.activity.addFilterDrawerItem(item);
            }
        }
    }

    public void removeTagFilterDrawerItem(Tag t) {
        //Have to add 2 for the Drawer components that reside above the actual tags' ui component.
        int pos = tags.indexOf(t) + 2;
        tags.remove(t);

        this.activity.removeFilterDrawerItem(pos);
    }

    public void updateTagFilterDrawerItem(Tag t) {

        if (this.tagsHelper != null) {

            //Add 2 for the same reason as above
            int pos = tags.indexOf(t) + 2;
            IDrawerItem item;

            if(this.editingTags) {
                item = new EditTagsDrawerItem()
                        .withName(t.getName())
                        .withTag(t);
            }else {
                item = new SwitchDrawerItem()
                        .withName(t.getName())
                        .withTag(t)
                        .withChecked(this.tagsHelper.isTagChecked(t.getId()))
                        .withOnCheckedChangeListener(this);
            }
            this.activity.updateFilterDrawerItem(item,pos);
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
        for (Map.Entry<Integer, TaskRecyclerViewFragment> tabEntry : ViewFragmentsDictionary.entrySet()) {
            if (tabEntry.getValue().getClassName().equals(taskType)) {
                viewPager.setCurrentItem(tabEntry.getKey());
            }
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

    public void showEditTagDialog(Tag tag) {

        Button btnDelete = null;

        final View editTagDialogView = this.activity.getLayoutInflater().inflate(R.layout.dialog_edit_tag,null);

        if(editTagDialogView != null) {
            EditText tagEditText = (EditText)editTagDialogView.findViewById(R.id.tagEditText);
            tagEditText.setText(tag.getName());

            btnDelete = (Button)editTagDialogView.findViewById(R.id.btnDelete);
            ViewHelper.SetBackgroundTint(btnDelete, ContextCompat.getColor(this.activity, R.color.worse_10));
        }

        AlertDialog alert = new AlertDialog.Builder(this.activity)
                .setTitle(getString(R.string.edit_tag_title))
                .setPositiveButton(getString(R.string.save_changes), null)
                .setNeutralButton(getString(R.string.dialog_go_back), (dialog, which) -> {
                    EditText tagEditText = (EditText)editTagDialogView.findViewById(R.id.tagEditText);
                    UiUtils.dismissKeyboard(this.activity,tagEditText);
                    dialog.cancel();
                })
                .create();
        btnDelete.setOnClickListener((View v) -> {showDeleteTagDialog(alert,tag);});
        alert.setView(editTagDialogView);
        alert.show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                EditText tagEditText = (EditText)editTagDialogView.findViewById(R.id.tagEditText);
                if(attemptUpdateTag(tagEditText,tag)) {
                    alert.dismiss();
                }
            }
        });
    }

    public boolean attemptUpdateTag(EditText tagEditText, Tag tag) {
        String newTagName = tagEditText.getText().toString();
        boolean dismiss = true;

        if(newTagName.equals("")) {
            dismiss = false;
            return dismiss;
        }

        UiUtils.dismissKeyboard(activity,tagEditText);

        if(newTagName.equals(tag.getName())) {
            return dismiss;
        }

        String uuid = tag.getId();
        tag.setName(newTagName);
        EventBus.getDefault().post(new UpdateTagCommand(tag, uuid));
        return dismiss;
    }

    public void showDeleteTagDialog(AlertDialog d, Tag tag) {
        AlertDialog confirmDeleteAlert = new AlertDialog.Builder(this.activity)
                .setTitle(getString(R.string.confirm_delete_tag_title)).setMessage(getString(R.string.confirm_delete_tag_message))
                .setPositiveButton(getString(R.string.yes),(dialog,which) -> {
                    EventBus.getDefault().post(new DeleteTagCommand(tag));
                    UiUtils.dismissKeyboard(this.activity,d.getCurrentFocus());
                    //dismiss both dialogs
                    dialog.dismiss();
                    d.dismiss();
                })
                .setNegativeButton(getString(R.string.no),(dialog, which) -> {
                    dialog.dismiss();
                })
                .create();
        confirmDeleteAlert.show();
    }
}
