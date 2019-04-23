package com.habitrpg.android.habitica.interactors

import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.executors.ThreadExecutor
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User

import javax.inject.Inject

import io.reactivex.Flowable

class TodoCheckUseCase @Inject
constructor(private val taskRepository: TaskRepository, private val soundManager: SoundManager,
            threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : UseCase<TodoCheckUseCase.RequestValues, TaskScoringResult>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: TodoCheckUseCase.RequestValues): Flowable<TaskScoringResult?> {
        return taskRepository.taskChecked(requestValues.user, requestValues.task, requestValues.up, false, requestValues.notifyFunc).doOnNext { soundManager.loadAndPlayAudio(SoundManager.SoundTodo) }
    }

    class RequestValues(var user: User?, val task: Task, up: Boolean, val notifyFunc: (TaskScoringResult) -> Unit) : UseCase.RequestValues {

        var up = false

        init {
            this.up = up
        }
    }
}
