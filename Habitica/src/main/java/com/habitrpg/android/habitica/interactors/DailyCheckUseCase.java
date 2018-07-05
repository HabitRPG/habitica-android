package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import javax.inject.Inject;

import io.reactivex.Flowable;

public class DailyCheckUseCase extends UseCase<DailyCheckUseCase.RequestValues, TaskScoringResult> {

    private TaskRepository taskRepository;
    private SoundManager soundManager;

    @Inject
    public DailyCheckUseCase(TaskRepository taskRepository, SoundManager soundManager,
                             ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.taskRepository = taskRepository;
        this.soundManager = soundManager;
    }

    @Override
    protected Flowable<TaskScoringResult> buildUseCaseObservable(RequestValues requestValues) {
        return taskRepository.taskChecked(requestValues.user, requestValues.task, requestValues.up, false).doOnNext(res -> soundManager.loadAndPlayAudio(SoundManager.SoundDaily));
    }

    public static final class RequestValues implements UseCase.RequestValues {

        protected boolean up = false;

        protected final Task task;
        public User user;

        public RequestValues(User user, Task task, boolean up) {
            this.user = user;
            this.task = task;
            this.up = up;
        }
    }
}
