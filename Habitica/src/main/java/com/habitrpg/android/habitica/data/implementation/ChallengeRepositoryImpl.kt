package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults


class ChallengeRepositoryImpl(localRepository: ChallengeLocalRepository, apiClient: ApiClient, private val userId: String) : BaseRepositoryImpl<ChallengeLocalRepository>(localRepository, apiClient), ChallengeRepository {
    override fun isChallengeMember(challengeID: String): Flowable<Boolean> {
        return localRepository.isChallengeMember(userId, challengeID)
    }

    override fun getChallengepMembership(id: String): Flowable<ChallengeMembership> {
        return localRepository.getChallengeMembership(userId, id)
    }

    override fun getChallengeMemberships(): Flowable<RealmResults<ChallengeMembership>> {
        return localRepository.getChallengeMemberships(userId)
    }

    override fun getChallenge(challengeId: String): Flowable<Challenge> {
        return localRepository.getChallenge(challengeId)
    }

    override fun getChallengeTasks(challengeId: String): Flowable<RealmResults<Task>> {
        return localRepository.getTasks(challengeId)
    }

    override fun retrieveChallenge(challengeID: String): Flowable<Challenge> {
        return apiClient.getChallenge(challengeID).doOnNext {
            localRepository.save(it)
        }
    }

    override fun retrieveChallengeTasks(challengeID: String): Flowable<TaskList> {
        return apiClient.getChallengeTasks(challengeID).doOnNext {
            val taskList = it.tasks.values.toList()
            taskList.forEach {
                it.userId = challengeID
            }
            localRepository.save(taskList)
        }
    }

    private fun getTaskOrders(taskList: List<Task>): TasksOrder {
        val stringListMap = taskList.groupBy { t -> t.type }

        val tasksOrder = TasksOrder()

        for ((key, value) in stringListMap) {
            val taskIdList = value.map { t -> t.id ?: "" }

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
        return when {
            addedTaskList.count() == 1 -> apiClient.createChallengeTask(challenge.id ?: "", addedTaskList[0]).map { challenge }
            addedTaskList.count() > 1 -> apiClient.createChallengeTasks(challenge.id ?: "", addedTaskList).map { challenge }
            else -> Flowable.just(challenge)
        }
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

    override fun leaveChallenge(challenge: Challenge, keepTasks: String): Flowable<Void> {
        return apiClient.leaveChallenge(challenge.id ?: "", LeaveChallengeBody(keepTasks))
                .doOnNext { localRepository.setParticipating(userId, challenge.id ?: "", false) }
    }

    override fun joinChallenge(challenge: Challenge): Flowable<Challenge> {
        return apiClient.joinChallenge(challenge.id ?: "")
                .doOnNext { localRepository.setParticipating(userId, challenge.id ?: "", true) }
    }
}
