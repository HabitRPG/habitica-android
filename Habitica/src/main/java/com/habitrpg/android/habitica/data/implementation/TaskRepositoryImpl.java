package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.models.Task;
import com.habitrpg.android.habitica.network.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public class TaskRepositoryImpl extends BaseRepositoryImpl<TaskLocalRepository> implements TaskRepository {


    public TaskRepositoryImpl(TaskLocalRepository localRepository, ApiService apiService) {
        super(localRepository, apiService);
    }

    @Override
    public Observable<ArrayList<Task>> getTasks(String taskType) {
        return this.localRepository.getTasks(taskType);
    }

    @Override
    public Observable<HabitResponse<ArrayList<Task>>> refreshTasks(TasksOrder tasksOrder) {
        return this.apiService.getUserTasks()
                .doOnNext(res -> this.localRepository.saveTasks(tasksOrder, res.data));
    }
}
