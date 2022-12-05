package com.habitrpg.android.habitica.interactors

import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import javax.inject.Inject

class BuyRewardUseCase @Inject
constructor(
    private val taskRepository: TaskRepository,
    private val soundManager: SoundManager,
) : FlowUseCase<BuyRewardUseCase.RequestValues, TaskScoringResult?>() {

    override suspend fun run(requestValues: RequestValues): TaskScoringResult? {
        val response = taskRepository.taskChecked(requestValues.user, requestValues.task, false, false, requestValues.notifyFunc)
        soundManager.loadAndPlayAudio(SoundManager.SoundReward)
        return response
    }

    class RequestValues(
        internal val user: User?,
        val task: Task,
        val notifyFunc: (TaskScoringResult) -> Unit
    ) : FlowUseCase.RequestValues
}
