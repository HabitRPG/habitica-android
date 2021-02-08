package com.habitrpg.android.habitica.data.local


import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task

import io.reactivex.rxjava3.core.Flowable
import io.realm.RealmResults

interface ChallengeLocalRepository : BaseLocalRepository {

    val challenges: Flowable<RealmResults<Challenge>>
    fun getChallenge(id: String): Flowable<Challenge>
    fun getTasks(challengeID: String): Flowable<RealmResults<Task>>

    fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>>

    fun setParticipating(userID: String, challengeID: String, isParticipating: Boolean)

    fun saveChallenges(challenges: List<Challenge>, clearChallenges: Boolean, memberOnly: Boolean)
    fun getChallengeMembership(userId: String, id: String): Flowable<ChallengeMembership>
    fun getChallengeMemberships(userId: String): Flowable<RealmResults<ChallengeMembership>>
    fun isChallengeMember(userID: String, challengeID: String): Flowable<Boolean>

}
