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
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

interface UserRepository : BaseRepository {
    fun getUser(): Flow<User?>
    fun getUserFlowable(): Flowable<User>
    fun getUser(userID: String): Flow<User?>

    suspend fun updateUser(updateData: Map<String, Any>): User?
    suspend fun updateUser(key: String, value: Any): User?

    suspend fun retrieveUser(withTasks: Boolean = false, forced: Boolean = false, overrideExisting: Boolean = false): User?

    suspend fun revive(): User?

    suspend fun resetTutorial(): User?

    suspend fun sleep(user: User): User?

    fun getSkills(user: User): Flowable<out List<Skill>>

    fun getSpecialItems(user: User): Flowable<out List<Skill>>

    suspend fun useSkill(key: String, target: String?, taskId: String): SkillResponse?
    suspend fun useSkill(key: String, target: String?): SkillResponse?

    suspend fun disableClasses(): User?
    suspend fun changeClass(selectedClass: String? = null): User?

    suspend fun unlockPath(path: String, price: Int): UnlockResponse?
    suspend fun unlockPath(customization: Customization): UnlockResponse?

    suspend fun runCron(tasks: MutableList<Task>)
    suspend fun runCron()

    fun readNotification(id: String): Flowable<List<Any>>
    fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>>
    fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>>

    fun changeCustomDayStart(dayStartTime: Int): Flowable<User>

    suspend fun updateLanguage(languageCode: String): User?

    suspend fun resetAccount(): User?
    fun deleteAccount(password: String): Flowable<Void>

    fun sendPasswordResetEmail(email: String): Flowable<Void>

    suspend fun updateLoginName(newLoginName: String, password: String? = null): User?
    fun updateEmail(newEmail: String, password: String): Flowable<Void>
    fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Flowable<Void>
    fun verifyUsername(username: String): Flowable<VerifyUsernameResponse>

    fun allocatePoint(stat: Attribute): Flowable<Stats>
    fun bulkAllocatePoints(strength: Int, intelligence: Int, constitution: Int, perception: Int): Flowable<Stats>

    suspend fun useCustomization(type: String, category: String?, identifier: String): User?
    fun retrieveAchievements(): Flowable<List<Achievement>>
    fun getAchievements(): Flow<List<Achievement>>
    fun getQuestAchievements(): Flow<List<QuestAchievement>>

    fun getUserQuestStatus(): Flowable<UserQuestStatus>

    suspend fun reroll(): User?
    fun retrieveTeamPlans(): Flowable<List<TeamPlan>>
    fun getTeamPlans(): Flow<List<TeamPlan>>
    suspend fun retrieveTeamPlan(teamID: String): Group?
    fun getTeamPlan(teamID: String): Flowable<Group>
}
