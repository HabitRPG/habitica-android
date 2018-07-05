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

public class HabitScoreUseCase extends UseCase<HabitScoreUseCase.RequestValues, TaskScoringResult> {

    private TaskRepository taskRepository;
    private SoundManager soundManager;

    @Inject
    public HabitScoreUseCase(TaskRepository taskRepository, SoundManager soundManager,
                             ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.taskRepository = taskRepository;
        this.soundManager = soundManager;
    }

    @Override
    protected Flowable<TaskScoringResult> buildUseCaseObservable(RequestValues requestValues) {
        return taskRepository
                .taskChecked(requestValues.user, requestValues.habit, requestValues.up, false)
                .doOnNext(res -> soundManager.loadAndPlayAudio(requestValues.up ? SoundManager.SoundPlusHabit : SoundManager.SoundMinusHabit));
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final User user;
        protected boolean up = false;

        protected final Task habit;

        public RequestValues(User user, Task habit, boolean up) {
            this.user = user;
            this.habit = habit;
            this.up = up;
        }
    }
}
