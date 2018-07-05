package com.habitrpg.android.habitica.data.implementation

import com.github.underscore.U
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults
import java.util.*


class ChallengeRepositoryImpl(localRepository: ChallengeLocalRepository, apiClient: ApiClient) : BaseRepositoryImpl<ChallengeLocalRepository>(localRepository, apiClient), ChallengeRepository {

    override fun getChallenge(challengeId: String): Flowable<Challenge> {
        return apiClient.getChallenge(challengeId)
    }

    override fun getChallengeTasks(challengeId: String): Flowable<TaskList> {
        return apiClient.getChallengeTasks(challengeId)
    }


    private fun getTaskOrders(taskList: List<Task>): TasksOrder {
        val stringListMap = U.groupBy(taskList) { t -> t.type }

        val tasksOrder = TasksOrder()

        for ((key, value) in stringListMap) {
            val taskIdList = U.map<String, Task>(value) { t -> t.id }

            when (key) {
                Task.TYPE_HABIT -> tasksOrder.habits = taskIdList
                Task.TYPE_DAILY -> tasksOrder.dailys = taskIdList
                Task.TYPE_TODO -> tasksOrder.todos = taskIdList
                Task.TYPE_REWARD -> tasksOrder.rewards = taskIdList
            }
        }

        return tasksOrder
    }

    private fun addChallengeTasks(challenge: Challenge, addedTaskList: List<Task>): Flowable<Challenge> {
        return apiClient.createChallengeTasks(challenge.id, addedTaskList).map { challenge }
    }

    override fun createChallenge(challenge: Challenge, taskList: List<Task>): Flowable<Challenge> {
        challenge.tasksOrder = getTaskOrders(taskList)

        return apiClient.createChallenge(challenge).flatMap {
            addChallengeTasks(it, taskList)
        }
    }

    override fun updateChallenge(challenge: Challenge, fullTaskList: List<Task>,
                                 addedTaskList: List<Task>, updatedTaskList: List<Task>, removedTaskList: List<String>): Flowable<Challenge> {

        var flowable: Flowable<*> = Flowable.just("")

        updatedTaskList.forEach { task ->
            flowable = flowable.flatMap { apiClient.updateTask(task.id ?: "", task) }
        }

        removedTaskList.forEach { task ->
            flowable = flowable.flatMap { apiClient.deleteTask(task) }
        }
        if (addedTaskList.isNotEmpty()) {
            flowable = flowable.flatMap { addChallengeTasks(challenge, addedTaskList) }
        }

        challenge.tasksOrder = getTaskOrders(fullTaskList)

        return flowable.flatMap { apiClient.updateChallenge(challenge) }
    }

    override fun deleteChallenge(challengeId: String): Flowable<Void> {
        return apiClient.deleteChallenge(challengeId)
    }

    override fun getChallenges(): Flowable<RealmResults<Challenge>> {
        return localRepository.challenges
    }

    override fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>> {
        return localRepository.getUserChallenges(userId)
    }

    override fun retrieveChallenges(user: User): Flowable<List<Challenge>> {
        return apiClient.userChallenges
                .doOnNext { localRepository.saveChallenges(it) }
    }

    override fun leaveChallenge(challenge: Challenge?, leaveChallengeBody: LeaveChallengeBody): Flowable<Void> {
        return if (challenge == null) {
            Flowable.empty()
        } else apiClient.leaveChallenge(challenge.id, leaveChallengeBody)
                .doOnNext { localRepository.setParticipating(challenge, false) }
    }

    override fun joinChallenge(challenge: Challenge?): Flowable<Challenge> {
        return if (challenge == null) {
            Flowable.empty()
        } else apiClient.joinChallenge(challenge.id)
                .doOnNext { localRepository.setParticipating(challenge, true) }
    }
}
