package com.habitrpg.android.habitica.ui.fragments.social.challenges;

import android.content.Context;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.menu.DividerItemDecoration;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import javax.inject.Inject;
import javax.inject.Named;

public class ChallengeTaskRecyclerViewFragment extends BaseFragment {
    public RecyclerView recyclerView;
    public BaseTasksRecyclerViewAdapter recyclerAdapter;
    @Inject
    @Named("UserID")
    String userID;
    @Inject
    APIHelper apiHelper;

    LinearLayoutManager layoutManager = null;
    private String classType;
    private HabitRPGUser user;
    private View view;

    public static ChallengeTaskRecyclerViewFragment newInstance(HabitRPGUser user, String classType, ObservableList<Task> tasks) {
        ChallengeTaskRecyclerViewFragment fragment = new ChallengeTaskRecyclerViewFragment();
        fragment.setRetainInstance(true);
        fragment.user = user;
        fragment.classType = classType;

        if(tasks.size() != 0 && fragment.recyclerAdapter != null){
            fragment.recyclerAdapter.setTasks(tasks);
        }

        tasks.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<Task>>() {
            @Override
            public void onChanged(ObservableList<Task> tasks) {
            }

            @Override
            public void onItemRangeChanged(ObservableList<Task> tasks, int i, int i1) {

            }

            @Override
            public void onItemRangeInserted(ObservableList<Task> tasks, int i, int i1) {
                fragment.recyclerAdapter.setTasks(tasks);
            }

            @Override
            public void onItemRangeMoved(ObservableList<Task> tasks, int i, int i1, int i2) {

            }


            @Override
            public void onItemRangeRemoved(ObservableList<Task> tasks, int i, int i1) {

            }
        });

        return fragment;
    }

    public void setInnerAdapter() {
        int layoutOfType;
        if (this.classType != null) {
            switch (this.classType) {
                case Task.TYPE_HABIT:
                    layoutOfType = R.layout.habit_item_card;
                    this.recyclerAdapter = new ChallengeHabitsRecyclerViewAdapter(Task.TYPE_HABIT, null, layoutOfType, getContext(), userID, null);
                    break;
                case Task.TYPE_DAILY:
                    layoutOfType = R.layout.daily_item_card;
                    int dailyResetOffset = 0;
                    if (user != null) {
                        dailyResetOffset = user.getPreferences().getDayStart();
                    }
                    this.recyclerAdapter = new ChallengeDailiesRecyclerViewHolder(Task.TYPE_DAILY, null, layoutOfType, getContext(), userID, dailyResetOffset, null);
                    break;
                case Task.TYPE_TODO:
                    layoutOfType = R.layout.todo_item_card;
                    this.recyclerAdapter = new ChallengeTodosRecyclerViewAdapter(Task.TYPE_TODO, null, layoutOfType, getContext(), userID, null);
                    return;
                case Task.TYPE_REWARD:
                    layoutOfType = R.layout.reward_item_card;
                    this.recyclerAdapter = new ChallengeRewardsRecyclerViewAdapter(Task.TYPE_REWARD, null, layoutOfType, getContext(), user);
                    break;
            }
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
        return this.classType + super.getDisplayedClassName();
    }

    String getClassName() {
        return classType;
    }

    // region Challenge specific RecyclerViewAdapters

    private class ChallengeHabitsRecyclerViewAdapter extends SortableTasksRecyclerViewAdapter<HabitViewHolder> {
        public ChallengeHabitsRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext, String userID, SortTasksCallback sortCallback) {
            super(taskType, tagsHelper, layoutResource, newContext, userID, sortCallback);
        }

        @Override
        public boolean loadFromDatabase() {
            return false;
        }

        @Override
        public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HabitViewHolder habitViewHolder = new HabitViewHolder(getContentView(parent));
            habitViewHolder.setDisabled(true);
            return habitViewHolder;
        }
    }

    public class ChallengeDailiesRecyclerViewHolder extends SortableTasksRecyclerViewAdapter<DailyViewHolder> {

        public int dailyResetOffset;

        public ChallengeDailiesRecyclerViewHolder(String taskType, TagsHelper tagsHelper, int layoutResource,
                                         Context newContext, String userID, int dailyResetOffset,
                                         SortTasksCallback sortTasksCallback) {
            super(taskType, tagsHelper, layoutResource, newContext, userID, sortTasksCallback);
            this.dailyResetOffset = dailyResetOffset;
        }

        @Override
        public boolean loadFromDatabase() {
            return false;
        }

        @Override
        public DailyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            DailyViewHolder dailyViewHolder = new DailyViewHolder(getContentView(parent), dailyResetOffset);
            dailyViewHolder.setDisabled(true);
            return dailyViewHolder;
        }
    }

    public class ChallengeTodosRecyclerViewAdapter extends SortableTasksRecyclerViewAdapter<TodoViewHolder> {

        public ChallengeTodosRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource,
                                        Context newContext, String userID, SortTasksCallback sortCallback) {
            super(taskType, tagsHelper, layoutResource, newContext, userID, sortCallback);
        }

        @Override
        public boolean loadFromDatabase() {
            return false;
        }

        @Override
        public TodoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TodoViewHolder todoViewHolder = new TodoViewHolder(getContentView(parent));
            todoViewHolder.setDisabled(true);
            return todoViewHolder;
        }
    }

    public class ChallengeRewardsRecyclerViewAdapter extends BaseTasksRecyclerViewAdapter<RewardViewHolder> {

        public ChallengeRewardsRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource, Context newContext, HabitRPGUser user) {
            super(taskType, tagsHelper, layoutResource, newContext, user.getId());
        }

        @Override
        public boolean loadFromDatabase() {
            return false;
        }

        @Override
        public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RewardViewHolder rewardViewHolder = new RewardViewHolder(getContentView(parent));
            rewardViewHolder.setDisabled(true);
            return rewardViewHolder;
        }
    }

    // endregion
}
