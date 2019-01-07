package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class RealmChallengeLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), ChallengeLocalRepository {

    override fun isChallengeMember(userID: String, challengeID: String): Flowable<Boolean> = realm.where(ChallengeMembership::class.java)
            .equalTo("userID", userID)
            .equalTo("challengeID", challengeID)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }
            .map { it.count() > 0 }

    override fun getChallengeMembership(userId: String, id: String): Flowable<ChallengeMembership> = realm.where(ChallengeMembership::class.java)
            .equalTo("userID", userId)
            .equalTo("challengeID", id)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }
            .map { it.first() }

    override fun getChallengeMemberships(userId: String): Flowable<RealmResults<ChallengeMembership>> = realm.where(ChallengeMembership::class.java)
            .equalTo("userID", userId)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }

    override fun getChallenge(id: String): Flowable<Challenge> {
        return realm.where(Challenge::class.java)
                .equalTo("id", id)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isNotEmpty() }
                .map { it.first() }
    }

    override fun getTasks(challengeID: String): Flowable<RealmResults<Task>> {
        return realm.where(Task::class.java)
                .equalTo("userId", challengeID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded }
    }

    override val challenges: Flowable<RealmResults<Challenge>>
        get() = realm.where(Challenge::class.java)
                .isNotNull("name")
                .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }

    override fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>> {
        return realm.where(ChallengeMembership::class.java)
                .equalTo("userID", userId)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .flatMap {
                    val ids = it.map {
                        return@map it.challengeID
                    }.toTypedArray()
                    realm.where(Challenge::class.java)
                            .isNotNull("name")
                            .`in`("id", ids)
                            .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                            .findAll()
                            .asFlowable()
                            .filter { it.isLoaded }
                }
    }

    override fun setParticipating(userID: String, challengeID: String, isParticipating: Boolean) {
        if (isParticipating) {
            realm.executeTransaction {
                realm.insertOrUpdate(ChallengeMembership(userID, challengeID))
            }
        } else {
            val membership = realm.where(ChallengeMembership::class.java).equalTo("userID", userID).equalTo("challengeID", challengeID).findFirst()
            if (membership != null) {
                realm.executeTransaction {
                    membership.deleteFromRealm()
                }
            }
        }
    }

    override fun saveChallenges(challenges: List<Challenge>, clearChallenges: Boolean, memberOnly: Boolean) {
        if (clearChallenges && !memberOnly) {
            val localChallenges = realm.where(Challenge::class.java).findAll().createSnapshot()
            val challengesToDelete = ArrayList<Challenge>()
            for (localTask in localChallenges) {
                if (!challenges.contains(localTask)) {
                    challengesToDelete.add(localTask)
                }
            }
            realm.executeTransaction {
                for (localTask in challengesToDelete) {
                    localTask.deleteFromRealm()
                }
            }
        }
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(challenges) }
    }
}
