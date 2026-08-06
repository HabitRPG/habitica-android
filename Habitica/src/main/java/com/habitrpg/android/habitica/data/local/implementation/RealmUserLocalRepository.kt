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
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import io.realm.Realm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RealmUserLocalRepository(
    realm: Realm,
) : RealmBaseLocalRepository(realm),
    UserLocalRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserQuestStatus(userID: String) =
        getUser(userID)
            .filterNotNull()
            .map { it.party?.id ?: "" }
            .filter { it.isNotBlank() }
            .flatMapLatest {
                safeFindOne { realm -> realm.where(Group::class.java).equalTo("id", it) }
            }.map {
                when {
                    it.quest?.members?.find { questMember -> questMember.key == userID } === null -> UserQuestStatus.NO_QUEST

                    it.quest
                        ?.progress
                        ?.collect
                        ?.isNotEmpty()
                        ?: false -> UserQuestStatus.QUEST_COLLECT

                    (it.quest?.progress?.hp ?: 0.0) > 0.0 -> UserQuestStatus.QUEST_BOSS

                    else -> UserQuestStatus.QUEST_UNKNOWN
                }
            }

    override fun updateDayStartTime(
        currentUserID: String,
        dayStartTime: Int
    ): User? {
        val user = getLiveUser(currentUserID) ?: return null
        executeTransaction {
            user.preferences?.dayStart = dayStartTime
        }
        return user
    }

    override fun updateStats(userID: String, stats: Stats) {
        val user = getLiveUser(userID) ?: return
        executeTransaction {
            stats.hp?.let { user.stats?.hp = it }
            stats.mp?.let { user.stats?.mp = it }
            stats.exp?.let { user.stats?.exp = it }
            stats.gp?.let { user.stats?.gp = it }
            stats.lvl?.let { user.stats?.lvl = it }
            stats.strength?.let { user.stats?.strength = it }
            stats.intelligence?.let { user.stats?.intelligence = it }
            stats.constitution?.let { user.stats?.constitution = it }
            stats.per?.let { user.stats?.per = it }
            stats.maxMP?.let { user.stats?.maxMP = it }
            stats.toNextLevel?.let { user.stats?.toNextLevel = it }
            stats.habitClass?.let { user.stats?.habitClass = it }
        }
    }

    override fun getAchievements(): Flow<List<Achievement>> = safeFindAll {
        it.where(Achievement::class.java).sort("index")
    }

    override fun getQuestAchievements(userID: String): Flow<List<QuestAchievement>> =
        queryUser(userID).map { it.questAchievements }

    override suspend fun getTutorialSteps() = safeFindAll { it.where(TutorialStep::class.java) }

    override fun getUser(userID: String): Flow<User?> = safeFindOne {
        it.where(User::class.java).equalTo("id", userID)
    }

    override fun saveUser(
        user: User,
        overrideExisting: Boolean,
    ) {
        val oldUser = safeQuery { it.where(User::class.java).equalTo("id", user.id) }?.findFirst()
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

    private fun removeOldTags(
        userId: String,
        onlineTags: List<Tag>,
    ) {
        val tags =
            safeQuery { it.where(Tag::class.java).equalTo("userId", userId) }
                ?.findAll()?.createSnapshot() ?: return
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

    override fun getTeamPlans(userID: String): Flow<List<TeamPlan>> = safeFindAll {
        it.where(TeamPlan::class.java).equalTo("userID", userID)
    }

    override fun getTeamPlan(teamID: String): Flow<Group?> = safeFindOne {
        it.where(Group::class.java).equalTo("id", teamID)
    }

    override fun getSkills(user: User): Flow<List<Skill>> {
        val habitClass =
            if (user.preferences?.disableClasses == true) "none" else user.stats?.habitClass
        return safeFindAll {
            it.where(Skill::class.java)
                .equalTo("habitClass", habitClass)
                .sort("lvl")
        }
    }

    override fun getSpecialItems(user: User): Flow<List<Skill>> {
        val specialItems = user.items?.special
        val ownedItems = ArrayList<String>()
        for (key in listOf("snowball", "shinySeed", "seafoam", "spookySparkles")) {
            if ((specialItems?.firstOrNull { it.key == key }?.numberOwned ?: 0) > 0) {
                ownedItems.add(key)
            }
        }
        return safeFindAll {
            it.where(Skill::class.java).`in`("key", ownedItems.toTypedArray())
        }
    }
}
