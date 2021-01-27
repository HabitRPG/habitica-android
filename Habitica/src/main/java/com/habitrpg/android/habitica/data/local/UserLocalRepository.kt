package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import io.realm.RealmResults

enum class UserQuestStatus {
    NO_QUEST,
    QUEST_COLLECT,
    QUEST_BOSS,
    QUEST_UNKNOWN
}

interface UserLocalRepository : BaseLocalRepository {

    fun getTutorialSteps(): Flowable<RealmResults<TutorialStep>>

    fun getUser(userID: String): Flowable<User>

    fun saveUser(user: User, overrideExisting: Boolean = true)

    fun saveMessages(messages: List<ChatMessage>)

    fun getSkills(user: User): Flowable<RealmResults<Skill>>

    fun getSpecialItems(user: User): Flowable<RealmResults<Skill>>
    fun getAchievements(): Flowable<RealmResults<Achievement>>
    fun getQuestAchievements(userID: String): Flowable<RealmResults<QuestAchievement>>
    fun getUserQuestStatus(userID: String): Flowable<UserQuestStatus>
    fun getTeamPlans(userID: String): Flowable<RealmResults<TeamPlan>>
    fun getTeamPlan(teamID: String): Flowable<Group>
}
