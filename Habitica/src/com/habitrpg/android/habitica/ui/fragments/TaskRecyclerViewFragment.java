package com.habitrpg.android.habitica.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.AddTaskTappedEvent;
import com.habitrpg.android.habitica.ui.adapter.HabitItemRecyclerViewAdapter;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;

import de.greenrobot.event.EventBus;

/**
 * TaskRecyclerViewFragment
 * - Creates the View only once
 * - Adds FAB Icon
 * - Handles the ScrollPosition - if anyone has a better solution please share it
 */
public class TaskRecyclerViewFragment extends Fragment implements View.OnClickListener {
    public RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Class<?> classType;
    private boolean showFloatingButton;

    // TODO needs a bit of cleanup
    public void SetInnerAdapter(HabitItemRecyclerViewAdapter adapter, Class<?> classType, boolean showFloatingButton) {
        this.classType = classType;
        this.showFloatingButton = showFloatingButton;
        mAdapter = new RecyclerViewMaterialAdapter(adapter);
        adapter.setParentAdapter(mAdapter);
    }

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        return view;
    }

    private boolean alreadyCreated;


    LinearLayoutManager layoutManager = null;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (alreadyCreated)
            return;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        android.support.v4.app.FragmentActivity context = getActivity();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        if (fab.getDrawable() == null) {
            IconicsDrawable icon = new IconicsDrawable(context, FontAwesome.Icon.faw_plus).color(Color.WHITE).sizeDp(24);

            fab.setImageDrawable(icon);
            fab.setOnClickListener(this);
            fab.setClickable(true);
        }

        fab.setVisibility(showFloatingButton ? View.VISIBLE : View.INVISIBLE);

        layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(context);

            mRecyclerView.setLayoutManager(layoutManager);
        }

        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setAdapter(mAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);

        alreadyCreated = true;
    }

    public static TaskRecyclerViewFragment newInstance(HabitItemRecyclerViewAdapter adapter, Class<?> classType) {
       return newInstance(adapter, classType, true);
    }

    public static TaskRecyclerViewFragment newInstance(HabitItemRecyclerViewAdapter adapter, Class<?> classType, boolean showFloatingButton) {
        TaskRecyclerViewFragment fragment = new TaskRecyclerViewFragment();

        fragment.SetInnerAdapter(adapter, classType,showFloatingButton);

        Log.d("TaskRecyclerViewFragment", "newInstance");

        return fragment;
    }

    @Override
    public void onClick(View v) {
        AddTaskTappedEvent event = new AddTaskTappedEvent();
        event.ClassType = classType;

        EventBus.getDefault().post(event);
    }
}
