package com.habitrpg.android.habitica.ui.fragments.skills;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.adapter.SkillTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import rx.Observable;

public class SkillTasksRecyclerViewFragment extends BaseFragment {
    @Inject
    TaskRepository taskRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    public SkillTasksRecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager = null;
    public String taskType;
    private View view;

    public SkillTasksRecyclerViewFragment() {
        super();
        adapter = new SkillTasksRecyclerViewAdapter(null, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        }

        return view;
    }
`
    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskRepository.getTasks(taskType, userId).first().subscribe(tasks -> adapter.updateData(tasks), RxErrorHandler.handleEmptyError());
        recyclerView.setAdapter(adapter);

        layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(getContext());

            recyclerView.setLayoutManager(layoutManager);
        }

    }

    public Observable<Task> getTaskSelectionEvents() {
        return adapter.getTaskSelectionEvents();
    }
}
