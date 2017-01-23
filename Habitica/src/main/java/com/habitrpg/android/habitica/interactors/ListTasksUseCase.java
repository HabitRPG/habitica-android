package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.events.HabitScoreEvent;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.Observable;

public class ListTasksUseCase extends UseCase<ListTasksUseCase.RequestValues, ArrayList<Task>> {

    private final TaskRepository taskRepository;

    @Inject
    public ListTasksUseCase(TaskRepository taskRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.taskRepository = taskRepository;
    }

    @Override
    protected Observable<ArrayList<Task>> buildUseCaseObservable(ListTasksUseCase.RequestValues requestValues) {
        return taskRepository.getTasks(requestValues.taskType);
    }

    public static final class RequestValues implements UseCase.RequestValues {

        protected final String taskType;

        public RequestValues(String taskType) {
            this.taskType = taskType;
        }
    }
}

