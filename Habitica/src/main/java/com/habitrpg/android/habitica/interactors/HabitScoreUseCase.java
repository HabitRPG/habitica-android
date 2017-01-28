package com.habitrpg.android.habitica.interactors;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import javax.inject.Inject;

import rx.Observable;

public class HabitScoreUseCase extends UseCase<HabitScoreUseCase.RequestValues, TaskDirectionData> {


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
    protected Observable<TaskDirectionData> buildUseCaseObservable(RequestValues requestValues) {
        return taskRepository.scoreHabit(requestValues.habit, requestValues.Up).doOnNext(res -> {

            soundManager.loadAndPlayAudio(requestValues.Up ? SoundManager.SoundPlusHabit : SoundManager.SoundMinusHabit);
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        protected boolean Up = false;

        protected final Task habit;

        public RequestValues(Task habit, boolean up) {
            this.habit = habit;
            this.Up = up;
        }
    }
}
