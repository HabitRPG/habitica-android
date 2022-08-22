package com.habitrpg.android.habitica.interactors

import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class BuyRewardUseCase @Inject
constructor(
    private val taskRepository: TaskRepository,
    private val soundManager: SoundManager,
    postExecutionThread: PostExecutionThread
) : UseCase<BuyRewardUseCase.RequestValues, TaskScoringResult?>(postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<TaskScoringResult?> {
        return taskRepository
            .taskChecked(requestValues.user, requestValues.task, false, false, requestValues.notifyFunc)
            .doOnNext { soundManager.loadAndPlayAudio(SoundManager.SoundReward) }
    }

    class RequestValues(
        internal val user: User?,
        val task: Task,
        val notifyFunc: (TaskScoringResult) -> Unit
    ) : UseCase.RequestValues
}
