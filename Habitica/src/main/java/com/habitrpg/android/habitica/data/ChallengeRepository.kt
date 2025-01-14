package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository : BaseRepository {
    suspend fun retrieveChallenges(
        page: Int = 0,
        memberOnly: Boolean
    ): List<Challenge>?

    fun getChallenges(): Flow<List<Challenge>>

    fun getChallenge(challengeId: String): Flow<Challenge>

    fun getChallengeTasks(challengeId: String): Flow<List<Task>>

    suspend fun retrieveChallenge(challengeID: String): Challenge?

    suspend fun retrieveChallengeTasks(challengeID: String): TaskList?

    suspend fun createChallenge(
        challenge: Challenge,
        taskList: List<Task>
    ): Challenge?

    /**
     *
     * @param challenge the challenge that will be updated
     * @param fullTaskList lists all tasks of the current challenge, to create the taskOrders
     * @param addedTaskList only the tasks to be added online
     * @param updatedTaskList only the updated ones
     * @param removedTaskList tasks that has be to be removed
     * @return Observable with the updated challenge
     */
    suspend fun updateChallenge(
        challenge: Challenge,
        fullTaskList: List<Task>,
        addedTaskList: List<Task>,
        updatedTaskList: List<Task>,
        removedTaskList: List<String>
    ): Challenge?

    suspend fun deleteChallenge(challengeId: String): Void?

    fun getUserChallenges(userId: String? = null): Flow<List<Challenge>>

    suspend fun leaveChallenge(
        challenge: Challenge,
        keepTasks: String
    ): Void?

    suspend fun joinChallenge(challenge: Challenge): Challenge?

    fun getChallengepMembership(id: String): Flow<ChallengeMembership>

    fun getChallengeMemberships(): Flow<List<ChallengeMembership>>

    fun isChallengeMember(challengeID: String): Flow<Boolean>

    suspend fun reportChallenge(
        challengeid: String,
        updateData: Map<String, String>
    ): Void?
}
