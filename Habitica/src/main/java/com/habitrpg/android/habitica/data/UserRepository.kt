package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.data.local.UserQuestStatus
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Attribute
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import kotlinx.coroutines.flow.Flow

interface UserRepository : BaseRepository {
    fun getUser(): Flow<User?>
    fun getUserFlowable(): Flowable<User>
    fun getUser(userID: String): Flow<User?>

    fun updateUser(updateData: Map<String, Any>): Flowable<User>
    fun updateUser(key: String, value: Any): Flowable<User>

    fun retrieveUser(withTasks: Boolean): Flowable<User>
    fun retrieveUser(
        withTasks: Boolean = false,
        forced: Boolean = false,
        overrideExisting: Boolean = false
    ): Flowable<User>

    fun revive(): Flowable<User>

    fun resetTutorial(): Maybe<User>

    fun sleep(user: User): Flowable<User>

    fun getSkills(user: User): Flowable<out List<Skill>>

    fun getSpecialItems(user: User): Flowable<out List<Skill>>

    fun useSkill(key: String, target: String?, taskId: String): Flowable<SkillResponse>
    fun useSkill(key: String, target: String?): Flowable<SkillResponse>

    fun changeClass(): Flowable<User>

    fun disableClasses(): Flowable<User>

    fun changeClass(selectedClass: String): Flowable<User>

    fun unlockPath(path: String, price: Int): Flowable<UnlockResponse>
    fun unlockPath(customization: Customization): Flowable<UnlockResponse>

    fun runCron(tasks: MutableList<Task>)
    fun runCron()

    fun readNotification(id: String): Flowable<List<Any>>
    fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>>
    fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>>

    fun changeCustomDayStart(dayStartTime: Int): Flowable<User>

    fun updateLanguage(languageCode: String): Flowable<User>

    fun resetAccount(): Flowable<User>
    fun deleteAccount(password: String): Flowable<Void>

    fun sendPasswordResetEmail(email: String): Flowable<Void>

    fun updateLoginName(newLoginName: String, password: String? = null): Maybe<User>
    fun updateEmail(newEmail: String, password: String): Flowable<Void>
    fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Flowable<Void>
    fun verifyUsername(username: String): Flowable<VerifyUsernameResponse>

    fun allocatePoint(stat: Attribute): Flowable<Stats>
    fun bulkAllocatePoints(strength: Int, intelligence: Int, constitution: Int, perception: Int): Flowable<Stats>

    fun useCustomization(type: String, category: String?, identifier: String): Flowable<User>
    fun retrieveAchievements(): Flowable<List<Achievement>>
    fun getAchievements(): Flow<List<Achievement>>
    fun getQuestAchievements(): Flow<List<QuestAchievement>>

    fun getUserQuestStatus(): Flowable<UserQuestStatus>

    fun reroll(): Flowable<User>
    fun retrieveTeamPlans(): Flowable<List<TeamPlan>>
    fun getTeamPlans(): Flow<List<TeamPlan>>
    fun retrieveTeamPlan(teamID: String): Flowable<Group>
    fun getTeamPlan(teamID: String): Flowable<Group>
}
