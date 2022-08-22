package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.reactivex.rxjava3.core.Flowable
import retrofit2.HttpException

class ChallengeRepositoryImpl(
    localRepository: ChallengeLocalRepository,
    apiClient: ApiClient,
    userID: String
) : BaseRepositoryImpl<ChallengeLocalRepository>(localRepository, apiClient, userID), ChallengeRepository {

    override fun isChallengeMember(challengeID: String): Flowable<Boolean> {
        return localRepository.isChallengeMember(userID, challengeID)
    }

    override fun getChallengepMembership(id: String): Flowable<ChallengeMembership> {
        return localRepository.getChallengeMembership(userID, id)
    }

    override fun getChallengeMemberships(): Flowable<out List<ChallengeMembership>> {
        return localRepository.getChallengeMemberships(userID)
    }

    override fun getChallenge(challengeId: String): Flowable<Challenge> {
        return localRepository.getChallenge(challengeId)
    }

    override fun getChallengeTasks(challengeId: String): Flowable<out List<Task>> {
        return localRepository.getTasks(challengeId)
    }

    override fun retrieveChallenge(challengeID: String): Flowable<Challenge> {
        return apiClient.getChallenge(challengeID).doOnNext {
            localRepository.save(it)
        }
            .doOnError {
                if (it is HttpException && it.code() == 404) {
                    localRepository.getChallenge(challengeID).firstElement().subscribe { challenge ->
                        localRepository.delete(challenge)
                    }
                }
            }
    }

    override fun retrieveChallengeTasks(challengeID: String): Flowable<TaskList> {
        return apiClient.getChallengeTasks(challengeID).doOnNext { tasks ->
            val taskList = tasks.tasks.values.toList()
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
            if (key == null) continue
            when (key) {
                TaskType.HABIT -> tasksOrder.habits = taskIdList
                TaskType.DAILY -> tasksOrder.dailys = taskIdList
                TaskType.TODO -> tasksOrder.todos = taskIdList
                TaskType.REWARD -> tasksOrder.rewards = taskIdList
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

    override fun updateChallenge(
        challenge: Challenge,
        fullTaskList: List<Task>,
        addedTaskList: List<Task>,
        updatedTaskList: List<Task>,
        removedTaskList: List<String>
    ): Flowable<Challenge> {

        var flowable: Flowable<*> = Flowable.just("")

        updatedTaskList
            .map { localRepository.getUnmanagedCopy(it) }
            .forEach { task ->
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
            .doOnNext { localRepository.save(challenge) }
    }

    override fun deleteChallenge(challengeId: String): Flowable<Void> {
        return apiClient.deleteChallenge(challengeId)
    }

    override fun getChallenges(): Flowable<out List<Challenge>> {
        return localRepository.challenges
    }

    override fun getUserChallenges(userId: String?): Flowable<out List<Challenge>> {
        return localRepository.getUserChallenges(userId ?: userID)
    }

    override fun retrieveChallenges(page: Int, memberOnly: Boolean): Flowable<List<Challenge>> {
        return apiClient.getUserChallenges(page, memberOnly)
            .doOnNext { localRepository.saveChallenges(it, page == 0, memberOnly, userID) }
    }

    override fun leaveChallenge(challenge: Challenge, keepTasks: String): Flowable<Void> {
        return apiClient.leaveChallenge(challenge.id ?: "", LeaveChallengeBody(keepTasks))
            .doOnNext { localRepository.setParticipating(userID, challenge.id ?: "", false) }
    }

    override fun joinChallenge(challenge: Challenge): Flowable<Challenge> {
        return apiClient.joinChallenge(challenge.id ?: "")
            .doOnNext { localRepository.setParticipating(userID, challenge.id ?: "", true) }
    }
}
