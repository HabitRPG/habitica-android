package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmResults

class RealmUserLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), UserLocalRepository {
    override fun getIsUserOnQuest(userID: String): Flowable<Boolean> {
        return getUser(userID)
                .map { it.party?.id ?: "" }
                .filter { it.isNotBlank() }
                .flatMap {
                    realm.where(Group::class.java)
                            .equalTo("id", it)
                            .findAll()
                            .asFlowable()
                            .filter { groups -> groups.size > 0 }
                            .map { groups -> groups.first() }
                }
                .map { it.quest?.members?.find { questMember -> questMember.key == userID } != null }
    }

    override fun getAchievements(): Flowable<RealmResults<Achievement>> {
        return realm.where(Achievement::class.java)
                .sort("index")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getQuestAchievements(userID: String): Flowable<RealmResults<QuestAchievement>> {
        return realm.where(QuestAchievement::class.java)
                .equalTo("userID", userID)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getTutorialSteps(): Flowable<RealmResults<TutorialStep>> = realm.where(TutorialStep::class.java).findAll().asFlowable()
                .filter { it.isLoaded }

    override fun getUser(userID: String): Flowable<User> {
        return realm.where(User::class.java)
                .equalTo("id", userID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { users -> users.first() }
    }

    override fun saveUser(user: User) {
        val oldUser = realm.where(User::class.java)
                .equalTo("id", user.id)
                .findFirst()
        if (oldUser != null && oldUser.isValid) {
            if (user.needsCron && !oldUser.needsCron) {
                if (user.lastCron?.before(oldUser.lastCron) == true) {
                    user.needsCron = false
                }
            }
        }
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(user) }
        removeOldTags(user.id ?: "", user.tags)
        if (user.challenges != null) {
            removeOldChallenges(user.id ?: "", user.challenges ?: emptyList())
        }
    }

    override fun saveMessages(messages: List<ChatMessage>) {
        realm.executeTransaction {
            it.insertOrUpdate(messages)
        }
    }

    private fun removeOldTags(userId: String, onlineTags: List<Tag>) {
        val tags = realm.where(Tag::class.java).equalTo("userId", userId).findAll().createSnapshot()
        val tagsToDelete = tags.filterNot { onlineTags.contains(it) }
        realm.executeTransaction {
            for (tag in tagsToDelete) {
                tag.deleteFromRealm()
            }
        }
    }

    private fun removeOldChallenges(userID: String, onlineChallenges: List<ChallengeMembership>) {
        val memberships = realm.where(ChallengeMembership::class.java).equalTo("userID", userID).findAll().createSnapshot()
        val membershipsToDelete = memberships.filterNot { onlineChallenges.contains(it) }
        realm.executeTransaction {
            membershipsToDelete.forEach {
                it.deleteFromRealm()
            }
        }
    }

    override fun getSkills(user: User): Flowable<RealmResults<Skill>> {
        val habitClass = if (user.preferences?.disableClasses == true) "none" else user.stats?.habitClass
        return realm.where(Skill::class.java)
                .equalTo("habitClass", habitClass)
                .lessThanOrEqualTo("lvl", user.stats?.lvl ?: 0)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
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
        return realm.where(Skill::class.java)
                .`in`("key", ownedItems.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }
}
