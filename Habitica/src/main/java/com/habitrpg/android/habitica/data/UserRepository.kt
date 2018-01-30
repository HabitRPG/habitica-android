package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.realm.RealmResults
import rx.Observable

interface UserRepository : BaseRepository {
    val user: Observable<User>
    val inboxOverviewList: Observable<RealmResults<ChatMessage>>

    fun getUser(userID: String): Observable<User>
    fun updateUser(user: User?, updateData: Map<String, Any>): Observable<User>
    fun updateUser(user: User?, key: String, value: Any): Observable<User>

    fun retrieveUser(withTasks: Boolean): Observable<User>
    fun retrieveUser(withTasks: Boolean = false, forced: Boolean = false): Observable<User>

    fun getInboxMessages(replyToUserID: String?): Observable<RealmResults<ChatMessage>>

    fun revive(user: User): Observable<User>

    fun resetTutorial(user: User?)

    fun sleep(user: User): Observable<User>

    fun getSkills(user: User): Observable<RealmResults<Skill>>

    fun getSpecialItems(user: User): Observable<RealmResults<Skill>>

    fun useSkill(user: User?, key: String, target: String, taskId: String): Observable<SkillResponse>

    fun useSkill(user: User?, key: String, target: String): Observable<SkillResponse>

    fun changeClass(): Observable<User>

    fun disableClasses(): Observable<User>

    fun changeClass(selectedClass: String): Observable<User>

    fun unlockPath(user: User, customization: Customization): Observable<UnlockResponse>
    fun unlockPath(user: User, set: CustomizationSet): Observable<UnlockResponse>

    fun runCron(tasks: MutableList<Task>)
    fun runCron()

    fun readNotification(id: String): Observable<List<*>>

    fun changeCustomDayStart(dayStartTime: Int): Observable<User>

    fun updateLanguage(user: User?, languageCode: String): Observable<User>

    fun resetAccount(): Observable<User>
    fun deleteAccount(password: String): Observable<Void>

    fun sendPasswordResetEmail(email: String): Observable<Void>

    fun updateLoginName(newLoginName: String, password: String): Observable<Void>
    fun updateEmail(newEmail: String, password: String): Observable<Void>
    fun updatePassword(newPassword: String, oldPassword: String, oldPasswordConfirmation: String): Observable<Void>

    fun allocatePoint(user: User?, @Stats.StatsTypes stat: String): Observable<Stats>

    fun bulkAllocatePoints(user: User?, strength: Int, intelligence: Int, constitution: Int, perception: Int): Observable<Stats>
}
