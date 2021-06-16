package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable

enum class UserQuestStatus {
    NO_QUEST,
    QUEST_COLLECT,
    QUEST_BOSS,
    QUEST_UNKNOWN
}

interface UserLocalRepository : BaseLocalRepository {

    fun getTutorialSteps(): Flowable<List<TutorialStep>>

    fun getUser(userID: String): Flowable<User>

    fun saveUser(user: User, overrideExisting: Boolean = true)

    fun saveMessages(messages: List<ChatMessage>)

    fun getSkills(user: User): Flowable<out List<Skill>>

    fun getSpecialItems(user: User): Flowable<out List<Skill>>
    fun getAchievements(): Flowable<out List<Achievement>>
    fun getQuestAchievements(userID: String): Flowable<out List<QuestAchievement>>
    fun getUserQuestStatus(userID: String): Flowable<UserQuestStatus>
    fun getTeamPlans(userID: String): Flowable<out List<TeamPlan>>
    fun getTeamPlan(teamID: String): Flowable<Group>
}
