package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.shared.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.TaskList

import io.reactivex.Flowable
import io.realm.RealmResults

interface ChallengeRepository : BaseRepository {

    fun retrieveChallenges(page: Int = 0, memberOnly: Boolean): Flowable<List<Challenge>>
    fun getChallenges(): Flowable<RealmResults<Challenge>>
    fun getChallenge(challengeId: String): Flowable<Challenge>
    fun getChallengeTasks(challengeId: String): Flowable<RealmResults<Task>>

    fun retrieveChallenge(challengeID: String): Flowable<Challenge>
    fun retrieveChallengeTasks(challengeID: String): Flowable<TaskList>
    fun createChallenge(challenge: Challenge, taskList: List<Task>): Flowable<Challenge>

    /**
     *
     * @param challenge the challenge that will be updated
     * @param fullTaskList lists all tasks of the current challenge, to create the taskOrders
     * @param addedTaskList only the tasks to be added online
     * @param updatedTaskList only the updated ones
     * @param removedTaskList tasks that has be to be removed
     * @return Observable with the updated challenge
     */
    fun updateChallenge(challenge: Challenge, fullTaskList: List<Task>,
                        addedTaskList: List<Task>, updatedTaskList: List<Task>, removedTaskList: List<String>): Flowable<Challenge>

    fun deleteChallenge(challengeId: String): Flowable<Void>
    fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>>


    fun leaveChallenge(challenge: Challenge, keepTasks: String): Flowable<Void>

    fun joinChallenge(challenge: Challenge): Flowable<Challenge>

    fun getChallengepMembership(id: String): Flowable<ChallengeMembership>
    fun getChallengeMemberships(): Flowable<RealmResults<ChallengeMembership>>
    fun isChallengeMember(challengeID: String): Flowable<Boolean>
}
