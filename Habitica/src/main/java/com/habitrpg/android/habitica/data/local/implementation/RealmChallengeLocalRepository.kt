package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.social.CategoryOption
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RealmChallengeLocalRepository(
    realm: Realm,
) : RealmBaseLocalRepository(realm),
    ChallengeLocalRepository {
    override fun isChallengeMember(
        userID: String,
        challengeID: String,
    ): Flow<Boolean> =
        safeFindAll { it.where(ChallengeMembership::class.java)
            .equalTo("userID", userID)
            .equalTo("challengeID", challengeID) }
            .map { it.count() > 0 }

    override fun getChallengeMembership(
        userId: String,
        id: String,
    ) = safeFindOne {
        it.where(ChallengeMembership::class.java)
            .equalTo("userID", userId)
            .equalTo("challengeID", id)
    }

    override fun getChallengeMemberships(userId: String) = safeFindAll {
        it.where(ChallengeMembership::class.java).equalTo("userID", userId)
    }

    override fun getChallenge(id: String): Flow<Challenge> = safeFindOne {
        it.where(Challenge::class.java).equalTo("id", id)
    }

    override fun getTasks(challengeID: String): Flow<List<Task>> = safeFindAll {
        it.where(Task::class.java)
            .equalTo("ownerID", challengeID)
    }

    override val challenges: Flow<List<Challenge>>
        get() = safeFindAll {
            it.where(Challenge::class.java)
                .isNotNull("name")
                .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserChallenges(userId: String): Flow<List<Challenge>> = safeFindAll {
        it.where(ChallengeMembership::class.java)
            .equalTo("userID", userId)
    }
            .flatMapLatest { it ->
                val ids =
                    it
                        .map {
                            return@map it.challengeID
                        }.toTypedArray()
                safeFindAll {
                    it.where(Challenge::class.java)
                        .isNotNull("name")
                        .beginGroup()
                        .`in`("id", ids)
                        .or()
                        .equalTo("leaderId", userId)
                        .endGroup()
                        .sort("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                }
            }

    override fun setParticipating(
        userID: String,
        challengeID: String,
        isParticipating: Boolean,
    ) {
        val user = safeQuery { it.where(User::class.java).equalTo("id", userID) }?.findFirst() ?: return
        executeTransaction {
            if (isParticipating) {
                user.challenges?.add(ChallengeMembership(userID, challengeID))
            } else {
                val membership =
                    user.challenges?.firstOrNull { it.challengeID == challengeID }
                        ?: return@executeTransaction
                user.challenges?.remove(membership)
            }
        }
    }

    override fun saveChallenges(
        challenges: List<Challenge>,
        clearChallenges: Boolean,
        memberOnly: Boolean,
        userID: String,
    ) {
        if (clearChallenges || memberOnly) {
            val localChallenges = realm.where(Challenge::class.java).findAll().createSnapshot()
            val memberships = realm.where(ChallengeMembership::class.java).findAll()
            val challengesToDelete = ArrayList<Challenge>()
            for (localTask in localChallenges) {
                if (!challenges.contains(localTask) &&
                    memberships.find { it.challengeID == localTask.id } == null &&
                    localTask.leaderId != userID
                ) {
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

    override fun getCategoryOptions(): Flow<List<CategoryOption>> = safeFindAll {
        it.where(CategoryOption::class.java)
    }
}
