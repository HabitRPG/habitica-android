package com.habitrpg.android.habitica.ui.fragments.social.challenges;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class ChallengeTasksRecyclerViewFragment extends BaseFragment {
    public RecyclerView recyclerView;
    public BaseTasksRecyclerViewAdapter recyclerAdapter;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userID;

    List<Task> tasksOnInitialize;

    LinearLayoutManager layoutManager = null;
    private User user;
    private View view;

    public static ChallengeTasksRecyclerViewFragment newInstance(User user, List<Task> tasks) {
        ChallengeTasksRecyclerViewFragment fragment = new ChallengeTasksRecyclerViewFragment();
        fragment.setRetainInstance(true);
        fragment.user = user;
        fragment.tasksOnInitialize = tasks;

        if (tasks.size() != 0 && fragment.recyclerAdapter != null) {
            fragment.recyclerAdapter.setTasks(tasks);
        }

        return fragment;
    }

    public void setInnerAdapter() {
        this.recyclerAdapter = new ChallengeTasksRecyclerViewAdapter(null, 0, getContext(), userID, null, true, true);

        if (user != null && user.getPreferences() != null) {
            this.recyclerAdapter.setDailyResetOffset(user.getPreferences().getDayStart());
        }

        if (tasksOnInitialize != null && tasksOnInitialize.size() != 0 && recyclerAdapter != null && recyclerAdapter.getItemCount() == 0) {
            recyclerAdapter.setTasks(tasksOnInitialize);
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
            recyclerView.setItemAnimator(new SafeDefaultItemAnimator());
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
    public String getDisplayedClassName() {
        return "ChallengeTasks" + super.getDisplayedClassName();
    }


    // region Challenge specific RecyclerViewAdapters
    // endregion
}
