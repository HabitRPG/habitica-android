package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RealmUserLocalRepository(realm: Realm) :
    RealmBaseLocalRepository(realm),
    UserLocalRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserQuestStatus(userID: String): Flow<UserQuestStatus> {
        return getUser(userID)
            .filterNotNull()
            .map { it.party?.id ?: "" }
            .filter { it.isNotBlank() }
            .flatMapLatest {
                realm.where(Group::class.java)
                    .equalTo("id", it)
                    .findAll()
                    .toFlow()
                    .filter { groups -> groups.size > 0 }
                    .map { it.firstOrNull() }
                    .filterNotNull()
            }
            .map {
                when {
                    it.quest?.members?.find { questMember -> questMember.key == userID } === null -> UserQuestStatus.NO_QUEST
                    it.quest?.progress?.collect?.isNotEmpty()
                        ?: false -> UserQuestStatus.QUEST_COLLECT
                    (it.quest?.progress?.hp ?: 0.0) > 0.0 -> UserQuestStatus.QUEST_BOSS
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

    override suspend fun getTutorialSteps() =
        realm.where(TutorialStep::class.java).findAll().toFlow()
            .filter { it.isLoaded }.map { it }

    override fun getUser(userID: String): Flow<User?> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(User::class.java)
            .equalTo("id", userID)
            .findAll()
            .toFlow()
            .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
            .map { users -> users.first() }
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

    override fun getTeamPlan(teamID: String): Flow<Group?> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(Group::class.java)
            .equalTo("id", teamID)
            .findAll()
            .toFlow()
            .filter { realmObject -> realmObject.isLoaded && realmObject.isValid }
            .map { teams -> teams.firstOrNull() }
    }

    override fun getSkills(user: User): Flow<List<Skill>> {
        val habitClass =
            if (user.preferences?.disableClasses == true) "none" else user.stats?.habitClass
        return realm.where(Skill::class.java)
            .equalTo("habitClass", habitClass)
            .sort("lvl")
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getSpecialItems(user: User): Flow<List<Skill>> {
        val specialItems = user.items?.special
        val ownedItems = ArrayList<String>()
        for (key in listOf("snowball", "shinySeed", "seafoam", "spookySparkles")) {
            if ((specialItems?.firstOrNull { it.key == key }?.numberOwned ?: 0) > 0) {
                ownedItems.add(key)
            }
        }
        return realm.where(Skill::class.java)
            .`in`("key", ownedItems.toTypedArray())
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }
}
