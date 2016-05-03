package com.habitrpg.android.habitica.ui.fragments.tasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.ui.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.adapter.tasks.HabitItemRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

/**
 * TaskRecyclerViewFragment
 * - Creates the View only once
 * - Adds FAB Icon
 * - Handles the ScrollPosition - if anyone has a better solution please share it
 */
public class TaskRecyclerViewFragment extends BaseFragment implements View.OnClickListener {
    public RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    private String classType;
    private static final String CLASS_TYPE_KEY = "CLASS_TYPE_KEY";

    // TODO needs a bit of cleanup
    public void SetInnerAdapter(HabitItemRecyclerViewAdapter adapter, String classType) {
        this.classType = classType;
        mAdapter = adapter;
    }

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

            android.support.v4.app.FragmentActivity context = getActivity();

            layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

            if (layoutManager == null) {
                layoutManager = new LinearLayoutManager(context);

                mRecyclerView.setLayoutManager(layoutManager);
            }
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }

        if (savedInstanceState != null){
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

    LinearLayoutManager layoutManager = null;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CLASS_TYPE_KEY, this.classType);
    }

    public static TaskRecyclerViewFragment newInstance(HabitItemRecyclerViewAdapter adapter, String classType) {
        TaskRecyclerViewFragment fragment = new TaskRecyclerViewFragment();
        fragment.setRetainInstance(true);

        fragment.SetInnerAdapter(adapter, classType);

        return fragment;
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
