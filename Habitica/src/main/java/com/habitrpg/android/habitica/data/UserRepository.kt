package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import com.habitrpg.shared.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.shared.habitica.models.tasks.Attribute
import kotlinx.coroutines.flow.Flow

interface UserRepository : BaseRepository {
    fun getUser(): Flow<User?>
    fun getUser(userID: String): Flow<User?>

    suspend fun updateUser(updateData: Map<String, Any?>): User?
    suspend fun updateUser(key: String, value: Any?): User?

    suspend fun retrieveUser(withTasks: Boolean = false, forced: Boolean = false, overrideExisting: Boolean = false): User?

    suspend fun revive(): User?

    suspend fun resetTutorial(): User?

    suspend fun sleep(user: User): User?

    fun getSkills(user: User): Flow<List<Skill>>

    fun getSpecialItems(user: User): Flow<List<Skill>>

    suspend fun useSkill(key: String, target: String?, taskId: String): SkillResponse?
    suspend fun useSkill(key: String, target: String?): SkillResponse?

    suspend fun disableClasses(): User?
    suspend fun changeClass(selectedClass: String? = null): User?

    suspend fun unlockPath(path: String, price: Int): UnlockResponse?
    suspend fun unlockPath(customization: Customization): UnlockResponse?

    suspend fun runCron(tasks: MutableList<Task>)
    suspend fun runCron()

    suspend fun readNotification(id: String): List<Any>?
    suspend fun readNotifications(notificationIds: Map<String, List<String>>): List<Any>?
    suspend fun seeNotifications(notificationIds: Map<String, List<String>>): List<Any>?

    suspend fun changeCustomDayStart(dayStartTime: Int): User?

    suspend fun updateLanguage(languageCode: String): User?

    suspend fun resetAccount(): User?
    suspend fun deleteAccount(password: String): Void?

    suspend fun sendPasswordResetEmail(email: String): Void?

    suspend fun updateLoginName(newLoginName: String, password: String? = null): User?
    suspend fun updateEmail(newEmail: String, password: String): Void?
    suspend fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Void?
    suspend fun verifyUsername(username: String): VerifyUsernameResponse?

    suspend fun allocatePoint(stat: Attribute): Stats?
    suspend fun bulkAllocatePoints(strength: Int, intelligence: Int, constitution: Int, perception: Int): Stats?

    suspend fun useCustomization(type: String, category: String?, identifier: String): User?
    suspend fun retrieveAchievements(): List<Achievement>?
    fun getAchievements(): Flow<List<Achievement>>
    fun getQuestAchievements(): Flow<List<QuestAchievement>>

    fun getUserQuestStatus(): Flow<UserQuestStatus>

    suspend fun reroll(): User?
    suspend fun retrieveTeamPlans(): List<TeamPlan>?
    fun getTeamPlans(): Flow<List<TeamPlan>>
    suspend fun retrieveTeamPlan(teamID: String): Group?
    fun getTeamPlan(teamID: String): Flow<Group?>
}
