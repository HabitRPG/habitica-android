package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import javax.inject.Inject;

import rx.Observable;

public class BuyRewardUseCase extends UseCase<BuyRewardUseCase.RequestValues, TaskScoringResult> {

    private TaskRepository taskRepository;
    private SoundManager soundManager;

    @Inject
    public BuyRewardUseCase(TaskRepository taskRepository, SoundManager soundManager,
                            ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.taskRepository = taskRepository;
        this.soundManager = soundManager;
    }

    @Override
    protected Observable<TaskScoringResult> buildUseCaseObservable(BuyRewardUseCase.RequestValues requestValues) {
        return taskRepository
                .taskChecked(requestValues.user, requestValues.task, false, false)
                .doOnNext(res -> soundManager.loadAndPlayAudio(SoundManager.SoundReward));
    }

    public static final class RequestValues implements UseCase.RequestValues {
        protected final Task task;
        private final User user;

        public RequestValues(User user, Task task) {
            this.user = user;
            this.task = task;
        }
    }
}
