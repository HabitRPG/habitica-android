package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import com.habitrpg.android.habitica.extensions.filterMap
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class RealmUserLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), UserLocalRepository {
    override fun getUserQuestStatus(userID: String): Flowable<UserQuestStatus> {
        return getUserFlowable(userID)
            .map { it.party?.id ?: "" }
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .flatMap {
                RxJavaBridge.toV3Flowable(
                    realm.where(Group::class.java)
                        .equalTo("id", it)
                        .findAll()
                        .asFlowable()
                        .filter { groups -> groups.size > 0 }
                ).filterMap { it.first() }
            }
            .map {
                when {
                    it.quest?.members?.find { questMember -> questMember.key == userID } === null -> UserQuestStatus.NO_QUEST
                    it.quest?.progress?.collect?.isNotEmpty() ?: false -> UserQuestStatus.QUEST_COLLECT
                    it.quest?.progress?.hp ?: 0.0 > 0.0 -> UserQuestStatus.QUEST_BOSS
                    else -> UserQuestStatus.QUEST_UNKNOWN
                }
            }
    }

    override fun getAchievements(): Flow<List<Achievement>> {
        return realm.where(Achievement::class.java)
                .sort("index")
                .findAll()
                .toFlow()
                .filter { it.isLoaded }
    }

    override fun getQuestAchievements(userID: String): Flow<List<QuestAchievement>> {
        return realm.where(User::class.java)
                .equalTo("id", userID)
                .findAll()
                .toFlow()
                .filter { it.isLoaded && it.size > 0 }
                .map { it.first()?.questAchievements ?: emptyList() }
    }

    override fun getTutorialSteps(): Flowable<List<TutorialStep>> = RxJavaBridge.toV3Flowable(
        realm.where(TutorialStep::class.java).findAll().asFlowable()
            .filter { it.isLoaded }.map { it }
    )

    override fun getUser(userID: String): Flow<User?> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(User::class.java)
            .equalTo("id", userID)
            .findAll()
            .toFlow()
            .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
            .map { users -> users.first() }
    }

    override fun getUserFlowable(userID: String): Flowable<User> {
        if (realm.isClosed) return Flowable.empty()
        return RxJavaBridge.toV3Flowable(
            realm.where(User::class.java)
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

    override fun saveMessages(messages: List<ChatMessage>) {
        executeTransaction {
            it.insertOrUpdate(messages)
        }
    }

    override fun getTeamPlans(userID: String): Flow<List<TeamPlan>> {
        return realm.where(TeamPlan::class.java)
                .equalTo("userID", userID)
                .findAll()
                .toFlow()
                .filter { it.isLoaded }
    }

    override fun getTeamPlan(teamID: String): Flowable<Group> {
        if (realm.isClosed) return Flowable.empty()
        return RxJavaBridge.toV3Flowable(
            realm.where(Group::class.java)
                .equalTo("id", teamID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { teams -> teams.first() }
        )
    }

    override fun getSkills(user: User): Flowable<out List<Skill>> {
        val habitClass = if (user.preferences?.disableClasses == true) "none" else user.stats?.habitClass
        return RxJavaBridge.toV3Flowable(
            realm.where(Skill::class.java)
                .equalTo("habitClass", habitClass)
                .sort("lvl")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
        )
    }

    override fun getSpecialItems(user: User): Flowable<out List<Skill>> {
        val specialItems = user.items?.special
        val ownedItems = ArrayList<String>()
        for (key in listOf("snowball", "shinySeed", "seafoam", "spookySparkles")) {
            if (specialItems?.firstOrNull() { it.key == key }?.numberOwned ?: 0 > 0) {
                ownedItems.add(key)
            }
        }
        return RxJavaBridge.toV3Flowable(
            realm.where(Skill::class.java)
                .`in`("key", ownedItems.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
        )
    }
}
