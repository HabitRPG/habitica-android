package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;

import rx.Observable;


public class TaskRepositoryImpl extends BaseRepositoryImpl<TaskLocalRepository> implements TaskRepository {


    private APIHelper apiHelper;

    public TaskRepositoryImpl(TaskLocalRepository localRepository, APIHelper apiHelper) {
        super(localRepository, apiHelper.apiService);
        this.apiHelper = apiHelper;
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

    @Override
    public Observable<TaskDirectionData> scoreHabit(Task task, boolean up) {
        return this.apiService.postTaskDirection(task.getId(), (up ? TaskDirection.up : TaskDirection.down).toString())
                .compose(apiHelper.configureApiCallObserver())
                .doOnNext(res -> {

                    // save local task changes
                    if (task != null && task.type != null && !task.type.equals("reward")) {
                        task.value = task.value + res.getDelta();

                        this.localRepository.saveTask(task);
                    }

                    // play sound
                });
    }
}
