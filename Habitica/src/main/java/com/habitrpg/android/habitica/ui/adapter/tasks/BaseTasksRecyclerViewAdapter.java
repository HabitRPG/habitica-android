package com.habitrpg.android.habitica.ui.adapter.tasks;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseTasksRecyclerViewAdapter<VH extends BaseTaskViewHolder>
        extends RecyclerView.Adapter<VH> {
    private final String userID;
    public String taskType;
    @Inject
    protected CrashlyticsProxy crashlyticsProxy;
    @Inject
    protected TaskRepository taskRepository;
    protected List<Task> content;
    protected List<Task> filteredContent;
    private int layoutResource;
    Context context;
    private TaskFilterHelper taskFilterHelper;

    public BaseTasksRecyclerViewAdapter(String taskType, TaskFilterHelper taskFilterHelper, int layoutResource,
                                        Context newContext, @Nullable String userID) {
        this.setHasStableIds(true);
        this.taskType = taskType;
        this.context = newContext.getApplicationContext();
        this.taskFilterHelper = taskFilterHelper;
        this.userID = userID;
        this.filteredContent = new ArrayList<>();
        injectThis(HabiticaBaseApplication.getComponent());

        if (loadFromDatabase()) {
            this.loadContent(true);
        }

        this.layoutResource = layoutResource;
    }

    protected abstract void injectThis(AppComponent component);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Task item = filteredContent.get(position);

        holder.bindHolder(item, position);

        /*if (this.displayedChecklist != null && ChecklistedViewHolder.class.isAssignableFrom(holder.getClass())) {
            ChecklistedViewHolder checklistedHolder = (ChecklistedViewHolder) holder;
            checklistedHolder.setDisplayChecklist(this.displayedChecklist == position);
        }*/
    }

    @Override
    public long getItemId(int position) {
        Task task = filteredContent.get(position);
        return task.getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return filteredContent != null ? filteredContent.size() : 0;
    }

    View getContentView(ViewGroup parent) {
        return getContentView(parent, layoutResource);
    }

    protected View getContentView(ViewGroup parent, int layoutResource) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
    }

    public void updateTask(Task task) {
        if (!taskType.equals(task.getType()))
            return;
        int i;
        for (i = 0; i < this.content.size(); ++i) {
            if (content.get(i).getId().equals(task.getId())) {
                break;
            }
        }
        if (i < content.size()) {
            content.set(i, task);
        }
        filter();
    }

    public void filter() {
        if (this.taskFilterHelper == null || this.taskFilterHelper.howMany(taskType) == 0) {
            filteredContent = content;
        } else {
            filteredContent = new ObservableArrayList<>();
            filteredContent.addAll(this.taskFilterHelper.filter(content));
        }

        this.notifyDataSetChanged();
    }

    public void loadContent(boolean forced) {
        if (this.content == null || forced) {
            taskRepository.getTasks(this.taskType, this.userID)
                    .flatMap(Observable::from)
                    .map(task -> {
                        task.parseMarkdown();
                        return task;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toList()
                    .subscribe(this::setTasks, crashlyticsProxy::logException);
        }
    }

    public void setTasks(List<Task> tasks) {
        this.content = new ObservableArrayList<>();
        this.content.addAll(tasks);
        filter();
    }

    public boolean loadFromDatabase() {
        return true;
    }

    public void checkTask(Task task, Boolean completed) {
        if (!taskType.equals(task.getType()))
            return;

        if (completed && task.getType().equals("todo")) {
            // remove from the list
            content.remove(task);
        }
        this.updateTask(task);
        filter();
    }

    public void addTask(Task task) {
        if (!taskType.equals(task.getType()))
            return;

        content.add(0, task);
        filter();
    }

    public void removeTask(String deletedTaskId) {
        Task taskToDelete = null;

        for (Task t : content) {
            if (t.getId().equals(deletedTaskId)) {
                taskToDelete = t;
                break;
            }
        }

        if (taskToDelete != null) {
            content.remove(taskToDelete);
            filter();
        }
    }
}
