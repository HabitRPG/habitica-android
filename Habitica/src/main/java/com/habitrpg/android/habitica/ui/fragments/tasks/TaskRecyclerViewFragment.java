package com.habitrpg.android.habitica.ui.fragments.tasks;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder;
import com.habitrpg.android.habitica.ui.adapter.tasks.HabitsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.TodosRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.menu.DividerItemDecoration;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * TaskRecyclerViewFragment
 * - Creates the View only once
 * - Adds FAB Icon
 * - Handles the ScrollPosition - if anyone has a better solution please share it
 */
public class TaskRecyclerViewFragment extends BaseFragment implements View.OnClickListener {
    private static final String CLASS_TYPE_KEY = "CLASS_TYPE_KEY";
    public RecyclerView recyclerView;
    public BaseTasksRecyclerViewAdapter recyclerAdapter;
    @Inject
    @Named("UserID")
    String userID;
    @Inject
    APIHelper apiHelper;
    @Inject
    TagsHelper tagsHelper;
    LinearLayoutManager layoutManager = null;
    private String classType;
    private HabitRPGUser user;
    private View view;

    public static TaskRecyclerViewFragment newInstance(HabitRPGUser user, String classType) {
        TaskRecyclerViewFragment fragment = new TaskRecyclerViewFragment();
        fragment.setRetainInstance(true);
        fragment.user = user;
        fragment.classType = classType;
        return fragment;
    }

    // TODO needs a bit of cleanup
    public void setInnerAdapter() {
        int layoutOfType;
        switch (this.classType) {
            case Task.TYPE_HABIT:
                layoutOfType = R.layout.habit_item_card;
                this.recyclerAdapter = new HabitsRecyclerViewAdapter(Task.TYPE_HABIT, tagsHelper, layoutOfType, getContext(), userID);
                break;
            case Task.TYPE_DAILY:
                layoutOfType = R.layout.daily_item_card;
                int dailyResetOffset = 0;
                if (user != null) {
                    dailyResetOffset = user.getPreferences().getDayStart();
                }
                this.recyclerAdapter = new DailiesRecyclerViewHolder(Task.TYPE_DAILY, tagsHelper, layoutOfType, getContext(), userID, dailyResetOffset);
                break;
            case Task.TYPE_TODO:
                layoutOfType = R.layout.todo_item_card;
                this.recyclerAdapter = new TodosRecyclerViewAdapter(Task.TYPE_TODO, tagsHelper, layoutOfType, getContext(), userID);
                return;
            case Task.TYPE_REWARD:
                layoutOfType = R.layout.reward_item_card;
                this.recyclerAdapter = new RewardsRecyclerViewAdapter(Task.TYPE_REWARD, tagsHelper, layoutOfType, getContext(), user, apiHelper);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

            android.support.v4.app.FragmentActivity context = getActivity();

            layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (layoutManager == null) {
                layoutManager = new LinearLayoutManager(context);

                recyclerView.setLayoutManager(layoutManager);
            }
            if (recyclerView.getAdapter() == null) {
                this.setInnerAdapter();
            }
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }

        if (savedInstanceState != null) {
            this.classType = savedInstanceState.getString(CLASS_TYPE_KEY, "");
        }

        switch (this.classType) {
            case Task.TYPE_HABIT: {
                this.tutorialStepIdentifier = "habits";
                this.tutorialText = getString(R.string.tutorial_habits);
                break;
            }
            case Task.FREQUENCY_DAILY: {
                this.tutorialStepIdentifier = "dailies";
                this.tutorialText = getString(R.string.tutorial_dailies);
                break;
            }
            case Task.TYPE_TODO: {
                this.tutorialStepIdentifier = "todos";
                this.tutorialText = getString(R.string.tutorial_todos);
                break;
            }
            case Task.TYPE_REWARD: {
                this.tutorialStepIdentifier = "rewards";
                this.tutorialText = getString(R.string.tutorial_rewards);
                break;
            }
        }

        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CLASS_TYPE_KEY, this.classType);
    }

    @Override
    public void onClick(View v) {
        AddNewTaskCommand event = new AddNewTaskCommand();
        event.ClassType = this.classType;

        EventBus.getDefault().post(event);
    }

    @Override
    public String getDisplayedClassName() {
        return this.classType + super.getDisplayedClassName();
    }
}
