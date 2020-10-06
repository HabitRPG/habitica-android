package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class RealmChallengeLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), ChallengeLocalRepository {

    override fun isChallengeMember(userID: String, challengeID: String): Flowable<Boolean> = RxJavaBridge.toV3Flowable(realm.where(ChallengeMembership::class.java)
            .equalTo("userID", userID)
            .equalTo("challengeID", challengeID)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }).map { it.count() > 0 }

    override fun getChallengeMembership(userId: String, id: String): Flowable<ChallengeMembership> = RxJavaBridge.toV3Flowable(realm.where(ChallengeMembership::class.java)
            .equalTo("userID", userId)
            .equalTo("challengeID", id)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }).map { it.first() }

    override fun getChallengeMemberships(userId: String): Flowable<RealmResults<ChallengeMembership>> = RxJavaBridge.toV3Flowable(realm.where(ChallengeMembership::class.java)
            .equalTo("userID", userId)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded })

    override fun getChallenge(id: String): Flowable<Challenge> {
        return RxJavaBridge.toV3Flowable(realm.where(Challenge::class.java)
                .equalTo("id", id)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isNotEmpty() }
                .map { it.first() })
    }

    override fun getTasks(challengeID: String): Flowable<RealmResults<Task>> {
        return RxJavaBridge.toV3Flowable(realm.where(Task::class.java)
                .equalTo("userId", challengeID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded })
    }

    override val challenges: Flowable<RealmResults<Challenge>>
        get() = RxJavaBridge.toV3Flowable(realm.where(Challenge::class.java)
                .isNotNull("name")
                .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })

    override fun getUserChallenges(userId: String): Flowable<RealmResults<Challenge>> {
        return RxJavaBridge.toV3Flowable(realm.where(ChallengeMembership::class.java)
                .equalTo("userID", userId)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
.flatMap {
                    val ids = it.map {
                        return@map it.challengeID
                    }.toTypedArray()
                    realm.where(Challenge::class.java)
                            .isNotNull("name")
                            .beginGroup()
                            .`in`("id", ids)
                            .or()
                            .equalTo("leaderId", userId)
                            .endGroup()
                            .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                            .findAll()
                            .asFlowable()
                            .filter { it.isLoaded }
}
    }

    override fun setParticipating(userID: String, challengeID: String, isParticipating: Boolean) {
        if (isParticipating) {
            save(ChallengeMembership(userID, challengeID))
        } else {
            val membership = realm.where(ChallengeMembership::class.java).equalTo("userID", userID).equalTo("challengeID", challengeID).findFirst()
            if (membership != null) {
                executeTransaction {
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
            executeTransaction {
                for (localTask in challengesToDelete) {
                    localTask.deleteFromRealm()
                }
            }
        }
        executeTransaction { realm1 -> realm1.insertOrUpdate(challenges) }
    }
}
