package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task

import java.util.ArrayList

import io.reactivex.Flowable
import io.realm.OrderedRealmCollectionSnapshot
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class RealmChallengeLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), ChallengeLocalRepository {

    override fun getChallenge(id: String): Flowable<Challenge> {
        return realm.where(Challenge::class.java)
                .equalTo("id", id)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded }
                .map { it.first() }
    }

    override fun getTasks(challenge: Challenge): Flowable<List<Task>> {
        return Flowable.empty()
    }

    override val challenges: Flowable<RealmResults<Challenge>>
        get() = realm.where(Challenge::class.java)
                .isNotNull("name")
                .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }

    override fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>> {
        return realm.where(Challenge::class.java)
                .isNotNull("name")
                .equalTo("isParticipating", true)
                .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun setParticipating(challenge: Challenge, isParticipating: Boolean) {
        realm.executeTransaction { challenge.isParticipating = isParticipating }
    }

    override fun saveChallenges(onlineChallenges: List<Challenge>) {
        val localChallenges = realm.where(Challenge::class.java).findAll().createSnapshot()
        val challengesToDelete = ArrayList<Challenge>()
        for (localTask in localChallenges) {
            if (!onlineChallenges.contains(localTask)) {
                challengesToDelete.add(localTask)
            }
        }
        realm.executeTransaction {
            for (localTask in challengesToDelete) {
                localTask.deleteFromRealm()
            }
        }
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(onlineChallenges) }
    }
}
