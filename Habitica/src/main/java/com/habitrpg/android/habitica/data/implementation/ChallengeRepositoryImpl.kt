package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import kotlinx.coroutines.flow.Flow

class ChallengeRepositoryImpl(
    localRepository: ChallengeLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler,
) : BaseRepositoryImpl<ChallengeLocalRepository>(localRepository, apiClient, authenticationHandler),
    ChallengeRepository {
    override fun isChallengeMember(challengeID: String): Flow<Boolean> {
        return localRepository.isChallengeMember(currentUserID, challengeID)
    }

    override suspend fun reportChallenge(
        challengeid: String,
        updateData: Map<String, String>,
    ): Void? {
        return apiClient.reportChallenge(challengeid, updateData)
    }

    override fun getChallengepMembership(id: String): Flow<ChallengeMembership> {
        return localRepository.getChallengeMembership(currentUserID, id)
    }

    override fun getChallengeMemberships(): Flow<List<ChallengeMembership>> {
        return localRepository.getChallengeMemberships(currentUserID)
    }

    override fun getChallenge(challengeId: String): Flow<Challenge> {
        return localRepository.getChallenge(challengeId)
    }

    override fun getChallengeTasks(challengeId: String): Flow<List<Task>> {
        return localRepository.getTasks(challengeId)
    }

    override suspend fun retrieveChallenge(challengeID: String): Challenge? {
        val challenge = apiClient.getChallenge(challengeID) ?: return null
        localRepository.save(challenge)
        return challenge
    }

    override suspend fun retrieveChallengeTasks(challengeID: String): TaskList? {
        val tasks = apiClient.getChallengeTasks(challengeID)
        if (tasks != null) {
            val taskList = tasks.tasks.values.toList()
            taskList.forEach {
                it.ownerID = challengeID
            }
            localRepository.save(taskList)
        }
        return tasks
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

    private suspend fun addChallengeTasks(
        challenge: Challenge,
        addedTaskList: List<Task>,
    ) {
        when {
            addedTaskList.count() == 1 ->
                apiClient.createChallengeTask(
                    challenge.id ?: "",
                    addedTaskList[0],
                )

            addedTaskList.count() > 1 ->
                apiClient.createChallengeTasks(
                    challenge.id ?: "",
                    addedTaskList,
                )
        }
    }

    override suspend fun createChallenge(
        challenge: Challenge,
        taskList: List<Task>,
    ): Challenge? {
        challenge.tasksOrder = getTaskOrders(taskList)

        val createdChallenge = apiClient.createChallenge(challenge)
        if (createdChallenge != null) {
            addChallengeTasks(createdChallenge, taskList)
        }
        return createdChallenge
    }

    override suspend fun updateChallenge(
        challenge: Challenge,
        fullTaskList: List<Task>,
        addedTaskList: List<Task>,
        updatedTaskList: List<Task>,
        removedTaskList: List<String>,
    ): Challenge? {
        updatedTaskList
            .map { localRepository.getUnmanagedCopy(it) }
            .forEach { task ->
                apiClient.updateTask(task.id ?: "", task)
            }

        removedTaskList.forEach { task ->
            apiClient.deleteTask(task)
        }
        if (addedTaskList.isNotEmpty()) {
            addChallengeTasks(challenge, addedTaskList)
        }

        challenge.tasksOrder = getTaskOrders(fullTaskList)

        val updatedChallenges = apiClient.updateChallenge(challenge)
        if (updatedChallenges != null) {
            localRepository.save(updatedChallenges)
        }
        return updatedChallenges
    }

    override suspend fun deleteChallenge(challengeId: String): Void? {
        return apiClient.deleteChallenge(challengeId)
    }

    override fun getChallenges(): Flow<List<Challenge>> {
        return localRepository.challenges
    }

    override fun getUserChallenges(userId: String?): Flow<List<Challenge>> {
        return localRepository.getUserChallenges(userId ?: currentUserID)
    }

    override suspend fun retrieveChallenges(
        page: Int,
        memberOnly: Boolean,
    ): List<Challenge>? {
        val challenges = apiClient.getUserChallenges(page, memberOnly)
        if (challenges != null) {
            localRepository.saveChallenges(challenges, page == 0, memberOnly, currentUserID)
        }
        return challenges
    }

    override suspend fun leaveChallenge(
        challenge: Challenge,
        keepTasks: String,
    ): Void? {
        apiClient.leaveChallenge(challenge.id ?: "", LeaveChallengeBody(keepTasks))
        localRepository.setParticipating(currentUserID, challenge.id ?: "", false)
        return null
    }

    override suspend fun joinChallenge(challenge: Challenge): Challenge? {
        val returnedChallenge = apiClient.joinChallenge(challenge.id ?: "") ?: return null
        localRepository.setParticipating(currentUserID, returnedChallenge.id ?: "", true)
        return returnedChallenge
    }
}
