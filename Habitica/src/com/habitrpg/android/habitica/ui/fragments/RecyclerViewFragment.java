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
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;

/**
RecyclerViewFragment
 - Creates the View only once
 - Adds FAB Icon
 - Handles the ScrollPosition - if anyone has a better solution please share it


 */
public class RecyclerViewFragment extends Fragment {
    public RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    public void SetInnerAdapter(RecyclerView.Adapter adapter, String tag) {
        mAdapter = new RecyclerViewMaterialAdapter(adapter);
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

        IconicsDrawable icon = new IconicsDrawable(context, FontAwesome.Icon.faw_plus).color(Color.WHITE).sizeDp(24);

        fab.setImageDrawable(icon);

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

    public static RecyclerViewFragment newInstance(RecyclerView.Adapter adapter, String tag) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();

        fragment.SetInnerAdapter(adapter, tag);

        Log.d("RecyclerViewFragment", "newInstance");

        return fragment;
    }
}
