package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import javax.inject.Inject;

import rx.Observable;

public class ChecklistCheckUseCase extends UseCase<ChecklistCheckUseCase.RequestValues, Task> {

    private TaskRepository taskRepository;

    @Inject
    public ChecklistCheckUseCase(TaskRepository taskRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.taskRepository = taskRepository;
    }

    @Override
    protected Observable<Task> buildUseCaseObservable(ChecklistCheckUseCase.RequestValues requestValues) {
        return taskRepository.scoreChecklistItem(requestValues.taskId, requestValues.itemId);
    }

    public static final class RequestValues implements UseCase.RequestValues {

        protected final String itemId;

        protected final String taskId;

        public RequestValues(String taskId, String itemId) {
            this.taskId = taskId;
            this.itemId = itemId;
        }
    }
}
