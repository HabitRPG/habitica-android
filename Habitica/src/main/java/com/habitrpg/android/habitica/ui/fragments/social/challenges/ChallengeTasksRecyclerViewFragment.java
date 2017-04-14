package com.habitrpg.android.habitica.ui.fragments.social.challenges;


import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.adapter.tasks.BaseTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.tasks.Task;

import android.content.Context;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;
import javax.inject.Named;

public class ChallengeTasksRecyclerViewFragment extends BaseFragment {
    public RecyclerView recyclerView;
    public BaseTasksRecyclerViewAdapter recyclerAdapter;
    @Inject
    @Named("UserID")
    String userID;
    @Inject
    ApiClient apiClient;

    ObservableList<Task> tasksOnInitialize;

    LinearLayoutManager layoutManager = null;
    private HabitRPGUser user;
    private View view;

    public static ChallengeTasksRecyclerViewFragment newInstance(HabitRPGUser user, ObservableList<Task> tasks) {
        ChallengeTasksRecyclerViewFragment fragment = new ChallengeTasksRecyclerViewFragment();
        fragment.setRetainInstance(true);
        fragment.user = user;
        fragment.tasksOnInitialize = tasks;

        if (tasks.size() != 0 && fragment.recyclerAdapter != null) {
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
        this.recyclerAdapter = new ChallengeTasksRecyclerViewAdapter(null, 0, getContext(), userID, null);

        if (tasksOnInitialize.size() != 0 && recyclerAdapter != null && recyclerAdapter.getItemCount() == 0) {
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

    public class ChallengeTasksRecyclerViewAdapter extends SortableTasksRecyclerViewAdapter<BaseTaskViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_HABIT = 1;
        private static final int TYPE_DAILY = 2;
        private static final int TYPE_TODO = 3;
        private static final int TYPE_REWARD = 4;

        private int dailyResetOffset = 0;

        public ChallengeTasksRecyclerViewAdapter(@Nullable TaskFilterHelper taskFilterHelper, int layoutResource, Context newContext, String userID, @Nullable SortTasksCallback sortCallback) {
            super("", taskFilterHelper, layoutResource, newContext, userID, sortCallback);

            if (user != null) {
                dailyResetOffset = user.getPreferences().getDayStart();
            }
        }

        @Override
        protected void injectThis(AppComponent component) {
            component.inject(this);
        }

        @Override
        public boolean loadFromDatabase() {
            return false;
        }

        @Override
        public int getItemViewType(int position) {
            Task task = this.filteredContent.get(position);

            if (task.type.equals(Task.TYPE_HABIT))
                return TYPE_HABIT;

            if (task.type.equals(Task.TYPE_DAILY))
                return TYPE_DAILY;

            if (task.type.equals(Task.TYPE_TODO))
                return TYPE_TODO;

            if (task.type.equals(Task.TYPE_REWARD))
                return TYPE_REWARD;

            return TYPE_HEADER;
        }

        @Override
        public BaseTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            BaseTaskViewHolder viewHolder;

            switch (viewType) {
                case TYPE_HABIT:
                    viewHolder = new HabitViewHolder(getContentView(parent, R.layout.habit_item_card));
                    break;
                case TYPE_DAILY:
                    viewHolder = new DailyViewHolder(getContentView(parent, R.layout.daily_item_card), dailyResetOffset);
                    break;
                case TYPE_TODO:
                    viewHolder = new TodoViewHolder(getContentView(parent, R.layout.todo_item_card));
                    break;
                case TYPE_REWARD:
                    viewHolder = new RewardViewHolder(getContentView(parent, R.layout.reward_item_card));
                    break;
                default:
                    viewHolder = new DividerViewHolder(getContentView(parent, R.layout.challenge_task_divider));
                    break;
            }

            //viewHolder.setDisabled(true);
            return viewHolder;
        }
    }

    private class DividerViewHolder extends BaseTaskViewHolder {

        private TextView divider_name;

        public DividerViewHolder(View itemView) {
            super(itemView, false);

            divider_name = (TextView) itemView.findViewById(R.id.divider_name);

            context = itemView.getContext();
        }

        @Override
        public void bindHolder(Task newTask, int position) {
            divider_name.setText(newTask.text);
        }
    }

    // endregion
}
