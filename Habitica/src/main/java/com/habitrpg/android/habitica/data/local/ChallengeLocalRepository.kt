package com.habitrpg.android.habitica.data.local


import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task

import io.reactivex.Flowable
import io.realm.RealmResults

interface ChallengeLocalRepository : BaseLocalRepository {

    val challenges: Flowable<RealmResults<Challenge>>
    fun getChallenge(id: String): Flowable<Challenge>
    fun getTasks(challenge: Challenge): Flowable<List<Task>>

    fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>>

    fun setParticipating(challenge: Challenge, isParticipating: Boolean)

    fun saveChallenges(challenges: List<Challenge>)
}
