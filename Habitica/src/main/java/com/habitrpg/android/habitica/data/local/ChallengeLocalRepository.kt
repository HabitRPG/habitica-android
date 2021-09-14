package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import io.reactivex.rxjava3.core.Flowable

interface ChallengeLocalRepository : BaseLocalRepository {

    val challenges: Flowable<out List<Challenge>>
    fun getChallenge(id: String): Flowable<Challenge>
    fun getTasks(challengeID: String): Flowable<out List<Task>>

    fun getUserChallenges(userId: String): Flowable<out List<Challenge>>

    fun setParticipating(userID: String, challengeID: String, isParticipating: Boolean)

    fun saveChallenges(challenges: List<Challenge>, clearChallenges: Boolean, memberOnly: Boolean, userID: String)
    fun getChallengeMembership(userId: String, id: String): Flowable<ChallengeMembership>
    fun getChallengeMemberships(userId: String): Flowable<out List<ChallengeMembership>>
    fun isChallengeMember(userID: String, challengeID: String): Flowable<Boolean>
}
