package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.ui.adapter.HabitItemRecyclerViewAdapter;

import de.greenrobot.event.EventBus;

/**
 * TaskRecyclerViewFragment
 * - Creates the View only once
 * - Adds FAB Icon
 * - Handles the ScrollPosition - if anyone has a better solution please share it
 */
public class TaskRecyclerViewFragment extends Fragment implements View.OnClickListener {
    public RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    LinearLayoutManager layoutManager = null;
    private String classType;
    private View view;

    public static TaskRecyclerViewFragment newInstance(HabitItemRecyclerViewAdapter adapter, String classType) {
        TaskRecyclerViewFragment fragment = new TaskRecyclerViewFragment();
        fragment.setRetainInstance(true);

        fragment.SetInnerAdapter(adapter, classType);

        return fragment;
    }

    // TODO needs a bit of cleanup
    public void SetInnerAdapter(HabitItemRecyclerViewAdapter adapter, String classType) {
        this.classType = classType;
        mAdapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        android.support.v4.app.FragmentActivity context = getActivity();

        layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(context);

            mRecyclerView.setLayoutManager(layoutManager);
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        AddNewTaskCommand event = new AddNewTaskCommand();
        event.ClassType = this.classType;

        EventBus.getDefault().post(event);
    }
}
