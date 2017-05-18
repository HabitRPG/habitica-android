package com.habitrpg.android.habitica.ui.fragments.tasks;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.AddNewTaskCommand;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder;
import com.habitrpg.android.habitica.ui.adapter.tasks.HabitsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.TaskRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.TodosRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;
import com.habitrpg.android.habitica.ui.helpers.ItemTouchHelperDropCallback;
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * TaskRecyclerViewFragment
 * - Creates the View only once
 * - Adds FAB Icon
 * - Handles the ScrollPosition - if anyone has a better solution please share it
 */
public class TaskRecyclerViewFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String CLASS_TYPE_KEY = "CLASS_TYPE_KEY";
    public TaskRecyclerViewAdapter recyclerAdapter;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userID;
    @Inject
    ApiClient apiClient;
    @Inject
    TaskFilterHelper taskFilterHelper;
    @Inject
    UserRepository userRepository;
    @Inject
    InventoryRepository inventoryRepository;
    @Inject
    TaskRepository taskRepository;

    LinearLayoutManager layoutManager = null;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    public RecyclerViewEmptySupport recyclerView;

    @BindView(R.id.empty_view)
    ViewGroup emptyView;
    @BindView(R.id.empty_view_title)
    TextView emptyViewTitle;
    @BindView(R.id.empty_view_description)
    TextView emptyViewDescription;

    @Nullable
    String classType;
    @Nullable
    private User user;
    private View view;
    @Nullable
    private ItemTouchHelper.Callback mItemTouchCallback;

    public static TaskRecyclerViewFragment newInstance(Context context, @Nullable User user, String classType) {
        TaskRecyclerViewFragment fragment = new TaskRecyclerViewFragment();
        fragment.setRetainInstance(true);
        fragment.user = user;
        fragment.classType = classType;
        List<String> tutorialTexts = null;
        switch (fragment.classType) {
            case Task.TYPE_HABIT: {
                fragment.tutorialStepIdentifier = "habits";
                tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_overview),
                        context.getString(R.string.tutorial_habits_1),
                        context.getString(R.string.tutorial_habits_2),
                        context.getString(R.string.tutorial_habits_3),
                        context.getString(R.string.tutorial_habits_4));
                break;
            }
            case Task.FREQUENCY_DAILY: {
                fragment.tutorialStepIdentifier = "dailies";
                tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_dailies_1),
                        context.getString(R.string.tutorial_dailies_2));
                break;
            }
            case Task.TYPE_TODO: {
                fragment.tutorialStepIdentifier = "todos";
                tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_todos_1),
                        context.getString(R.string.tutorial_todos_2));
                break;
            }
            case Task.TYPE_REWARD: {
                fragment.tutorialStepIdentifier = "rewards";
                tutorialTexts = Arrays.asList(context.getString(R.string.tutorial_rewards_1),
                        context.getString(R.string.tutorial_rewards_2));
                break;
            }
        }

        if (tutorialTexts != null) {
            fragment.tutorialTexts = new ArrayList<>(tutorialTexts);
        }
        fragment.tutorialCanBeDeferred = false;

        return fragment;
    }

    // TODO needs a bit of cleanup
    public void setInnerAdapter() {
        if (this.classType != null) {
            taskRepository.getTasks(this.classType, userID).first().subscribe(tasks -> {
                int layoutOfType;
                switch (this.classType) {
                    case Task.TYPE_HABIT:
                        layoutOfType = R.layout.habit_item_card;
                        this.recyclerAdapter = new HabitsRecyclerViewAdapter(tasks, true, layoutOfType, taskFilterHelper);
                        allowReordering();
                        break;
                    case Task.TYPE_DAILY:
                        layoutOfType = R.layout.daily_item_card;
                        int dailyResetOffset = 0;
                        if (user != null) {
                            dailyResetOffset = user.getPreferences().getDayStart();
                        }
                        this.recyclerAdapter = new DailiesRecyclerViewHolder(tasks, true, layoutOfType, dailyResetOffset, taskFilterHelper);
                        allowReordering();
                        break;
                    case Task.TYPE_TODO:
                        layoutOfType = R.layout.todo_item_card;
                        this.recyclerAdapter = new TodosRecyclerViewAdapter(tasks, true, layoutOfType, taskFilterHelper);
                        allowReordering();
                        return;
                    case Task.TYPE_REWARD:
                        layoutOfType = R.layout.reward_item_card;
                        this.recyclerAdapter = new RewardsRecyclerViewAdapter(tasks, getContext(), layoutOfType, inventoryRepository, user);
                        break;
                }
            });
        }
    }

    private void allowReordering() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (Task.TYPE_DAILY.equals(classType)) {
            if (user != null && user.getPreferences().getDailyDueDefaultView()) {
                taskFilterHelper.setActiveFilter(Task.TYPE_DAILY, Task.FILTER_ACTIVE);
            }
        }

        mItemTouchCallback = new ItemTouchHelper.Callback() {
            private Integer mFromPosition = null;

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (viewHolder != null) {
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setEnabled(false);
                }
            }

            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if (mFromPosition == null) {
                    mFromPosition = viewHolder.getAdapterPosition();
                }
                taskRepository.swapTaskPosition(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setEnabled(true);
                }
                
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
                if (mFromPosition != null) {
                    taskRepository.updateTaskPosition(viewHolder.getAdapterPosition())
                            .subscribe(taskPositions -> {
                            }, RxErrorHandler.handleEmptyError());
                }
            }
        };

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_refresh_recyclerview, container, false);

            ButterKnife.bind(this, view);

            android.support.v4.app.FragmentActivity context = getActivity();

            layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (layoutManager == null) {
                layoutManager = new LinearLayoutManager(context);

                recyclerView.setLayoutManager(layoutManager);
            }
            if (recyclerView.getAdapter() == null) {
                this.setInnerAdapter();
            }

            int bottomPadding = (int) (recyclerView.getPaddingBottom() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));
            recyclerView.setPadding(0, 0, 0, bottomPadding);

            swipeRefreshLayout.setOnRefreshListener(this);

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && swipeRefreshLayout != null) {
                        swipeRefreshLayout.setEnabled(((MainActivity)getActivity()).isAppBarExpanded());
                    }
                }
            });

            if (this.classType != null) {
                switch (this.classType) {
                    case Task.TYPE_HABIT: {
                        this.emptyViewTitle.setText(R.string.empty_title_habits);
                        this.emptyViewDescription.setText(R.string.empty_description_habits);
                        break;
                    }
                    case Task.FREQUENCY_DAILY: {
                        this.emptyViewTitle.setText(R.string.empty_title_dailies);
                        this.emptyViewDescription.setText(R.string.empty_description_dailies);
                        break;
                    }
                    case Task.TYPE_TODO: {
                        this.emptyViewTitle.setText(R.string.empty_title_todos);
                        this.emptyViewDescription.setText(R.string.empty_description_todos);
                        break;
                    }
                    case Task.TYPE_REWARD: {
                        this.emptyViewTitle.setText(R.string.empty_title_rewards);
                        break;
                    }
                }
            }
        }

        if (savedInstanceState != null) {
            this.classType = savedInstanceState.getString(CLASS_TYPE_KEY, "");
        }

        return view;
    }

    @Override
    public void onDestroy() {
        userRepository.close();
        inventoryRepository.close();
        taskRepository.close();
        super.onDestroy();
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter((RecyclerView.Adapter) recyclerAdapter);
        if (recyclerAdapter != null) {
            recyclerAdapter.filter();
        }

        if (Task.TYPE_REWARD.equals(classType)) {
            compositeSubscription.add(taskRepository.getTasks(this.classType, userID)
                    .subscribe(recyclerAdapter::updateData, RxErrorHandler.handleEmptyError()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CLASS_TYPE_KEY, this.classType);
    }

    @Override
    public void onClick(View v) {
        AddNewTaskCommand event = new AddNewTaskCommand();
        event.taskType = this.classType;

        EventBus.getDefault().post(event);
    }

    @Override
    public String getDisplayedClassName() {
        return this.classType + super.getDisplayedClassName();
    }

    String getClassName() {
        return this.classType != null ? this.classType : "";
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        userRepository.retrieveUser(true, true)
                .doOnTerminate(() -> swipeRefreshLayout.setRefreshing(false))
                .subscribe(user1 -> {}, RxErrorHandler.handleEmptyError());
    }

    public void setActiveFilter(String activeFilter) {
        if (classType != null) {
            taskFilterHelper.setActiveFilter(classType, activeFilter);
        }
        recyclerAdapter.filter();
    }
}
