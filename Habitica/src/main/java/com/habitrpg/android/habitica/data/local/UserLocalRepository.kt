package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import io.realm.RealmResults
import kotlinx.coroutines.flow.Flow

interface UserLocalRepository : BaseLocalRepository {
    suspend fun getTutorialSteps(): Flow<RealmResults<TutorialStep>>

    fun getUser(userID: String): Flow<User?>

    fun saveUser(
        user: User,
        overrideExisting: Boolean = true,
    )

    fun saveMessages(messages: List<ChatMessage>)

    fun getSkills(user: User): Flow<List<Skill>>

    fun getSpecialItems(user: User): Flow<List<Skill>>

    fun getAchievements(): Flow<List<Achievement>>

    fun getQuestAchievements(userID: String): Flow<List<QuestAchievement>>

    fun getUserQuestStatus(userID: String): Flow<UserQuestStatus>

    fun getTeamPlans(userID: String): Flow<List<TeamPlan>>

    fun getTeamPlan(teamID: String): Flow<Group?>
}
