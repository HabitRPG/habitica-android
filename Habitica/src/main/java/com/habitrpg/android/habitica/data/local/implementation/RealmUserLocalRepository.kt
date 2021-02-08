package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.data.local.UserQuestStatus
import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm
import io.realm.RealmResults

class RealmUserLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), UserLocalRepository {
    override fun getUserQuestStatus(userID: String): Flowable<UserQuestStatus> {
        return getUser(userID)
                .map { it.party?.id ?: "" }
                .filter { it.isNotBlank() }
                .flatMap {
                    RxJavaBridge.toV3Flowable(realm.where(Group::class.java)
                            .equalTo("id", it)
                            .findAll()
                            .asFlowable()
                            .filter { groups -> groups.size > 0 }
                            .map { groups -> groups.first() })
                }
                .map { when {
                    it?.quest?.members?.find { questMember -> questMember.key == userID } === null -> UserQuestStatus.NO_QUEST
                    it.quest?.progress?.collect?.isNotEmpty() ?: false -> UserQuestStatus.QUEST_COLLECT
                    it.quest?.progress?.hp ?: 0.0 > 0.0 -> UserQuestStatus.QUEST_BOSS
                    else -> UserQuestStatus.QUEST_UNKNOWN
                }}
    }

    override fun getAchievements(): Flowable<RealmResults<Achievement>> {
        return RxJavaBridge.toV3Flowable(realm.where(Achievement::class.java)
                .sort("index")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getQuestAchievements(userID: String): Flowable<RealmResults<QuestAchievement>> {
        return RxJavaBridge.toV3Flowable(realm.where(QuestAchievement::class.java)
                .equalTo("userID", userID)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
}

    override fun getTutorialSteps(): Flowable<RealmResults<TutorialStep>> = RxJavaBridge.toV3Flowable(realm.where(TutorialStep::class.java).findAll().asFlowable()
                .filter { it.isLoaded })

    override fun getUser(userID: String): Flowable<User> {
        if (realm.isClosed) return Flowable.empty()
        return RxJavaBridge.toV3Flowable(realm.where(User::class.java)
                .equalTo("id", userID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { users -> users.first() })
    }

    override fun saveUser(user: User, overrideExisting: Boolean) {
        if (realm.isClosed) return
        val oldUser = realm.where(User::class.java)
                .equalTo("id", user.id)
                .findFirst()
        if (oldUser != null && oldUser.isValid) {
            if (user.needsCron && !oldUser.needsCron) {
                if (user.lastCron?.before(oldUser.lastCron) == true) {
                    user.needsCron = false
                }
            } else {
                if (oldUser.versionNumber >= user.versionNumber && !overrideExisting) {
                    return
                }
            }
        }
        executeTransaction { realm1 -> realm1.insertOrUpdate(user) }
        removeOldTags(user.id ?: "", user.tags)
        if (user.challenges != null) {
            removeOldChallenges(user.id ?: "", user.challenges ?: emptyList())
        }
        removeOldPets(user.id ?: "", user.items?.pets ?: emptyList())
        removeOldMounts(user.id ?: "", user.items?.mounts ?: emptyList())
    }

    override fun saveMessages(messages: List<ChatMessage>) {
        executeTransaction {
            it.insertOrUpdate(messages)
        }
    }

    private fun removeOldTags(userId: String, onlineTags: List<Tag>) {
        val tags = realm.where(Tag::class.java).equalTo("userId", userId).findAll().createSnapshot()
        val tagsToDelete = tags.filterNot { onlineTags.contains(it) }
        executeTransaction {
            for (tag in tagsToDelete) {
                tag.deleteFromRealm()
            }
        }
    }

    private fun removeOldChallenges(userID: String, onlineChallenges: List<ChallengeMembership>) {
        val memberships = realm.where(ChallengeMembership::class.java).equalTo("userID", userID).findAll().createSnapshot()
        val membershipsToDelete = memberships.filterNot { onlineChallenges.contains(it) }
        executeTransaction {
            membershipsToDelete.forEach {
                it.deleteFromRealm()
            }
        }
    }

    private fun removeOldPets(userID: String, onlinePets: List<OwnedPet>) {
        val pets = realm.where(OwnedPet::class.java).equalTo("userID", userID).findAll().createSnapshot()
        val petsToDelete = pets.filterNot { onlinePets.contains(it) }
        executeTransaction {
            petsToDelete.forEach {
                it.deleteFromRealm()
            }
        }
    }

    private fun removeOldMounts(userID: String, onlineMounts: List<OwnedMount>) {
        val mount = realm.where(OwnedMount::class.java).equalTo("userID", userID).findAll().createSnapshot()
        val mountsToDelete = mount.filterNot { onlineMounts.contains(it) }
        executeTransaction {
            mountsToDelete.forEach {
                it.deleteFromRealm()
            }
        }
    }

    override fun getTeamPlans(userID: String): Flowable<RealmResults<TeamPlan>> {
        return RxJavaBridge.toV3Flowable(realm.where(TeamPlan::class.java)
                .equalTo("userID", userID)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getTeamPlan(teamID: String): Flowable<Group> {
        if (realm.isClosed) return Flowable.empty()
        return RxJavaBridge.toV3Flowable(realm.where(Group::class.java)
                .equalTo("id", teamID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { teams -> teams.first() })
    }

    override fun getSkills(user: User): Flowable<RealmResults<Skill>> {
        val habitClass = if (user.preferences?.disableClasses == true) "none" else user.stats?.habitClass
        return RxJavaBridge.toV3Flowable(realm.where(Skill::class.java)
                .equalTo("habitClass", habitClass)
                .sort("lvl")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
}

    override fun getSpecialItems(user: User): Flowable<RealmResults<Skill>> {
        val specialItems = user.items?.special
        val ownedItems = ArrayList<String>()
        if (specialItems != null) {
            if (specialItems.snowball > 0) {
                ownedItems.add("snowball")
            }
            if (specialItems.shinySeed > 0) {
                ownedItems.add("shinySeed")
            }
            if (specialItems.seafoam > 0) {
                ownedItems.add("seafoam")
            }
            if (specialItems.spookySparkles > 0) {
                ownedItems.add("spookySparkles")
            }
        }
        if (ownedItems.size == 0) {
            ownedItems.add("")
        }
        return RxJavaBridge.toV3Flowable(realm.where(Skill::class.java)
                .`in`("key", ownedItems.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
}
}
