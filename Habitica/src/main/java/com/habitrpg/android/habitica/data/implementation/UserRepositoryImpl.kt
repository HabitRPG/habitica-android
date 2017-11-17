package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.realm.RealmResults
import rx.Observable
import rx.functions.Action1
import java.util.*
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(localRepository: UserLocalRepository, apiClient: ApiClient, private val userId: String, private val taskRepository: TaskRepository) : BaseRepositoryImpl<UserLocalRepository>(localRepository, apiClient), UserRepository {

    private var lastSync: Date? = null

    override val user: Observable<User>
        get() = getUser(userId) //To change initializer of created properties use File | Settings | File Templates.
    override val inboxOverviewList: Observable<RealmResults<ChatMessage>>
        get() = localRepository.getInboxOverviewList(userId) //To change initializer of created properties use File | Settings | File Templates.


    override fun getUser(userID: String): Observable<User> = localRepository.getUser(userID)

    override fun updateUser(user: User?, updateData: Map<String, Any>): Observable<User> {
        return if (user == null) {
            Observable.just(User())
        } else apiClient.updateUser(updateData).map { newUser -> mergeUser(user, newUser) }
    }

    override fun updateUser(user: User?, key: String, value: Any): Observable<User> {
        val updateData = HashMap<String, Any>()
        updateData.put(key, value)
        return updateUser(user, updateData)
    }

    override fun retrieveUser(withTasks: Boolean): Observable<User> =
            retrieveUser(withTasks, false)

    override fun retrieveUser(withTasks: Boolean, forced: Boolean): Observable<User> {
        if (forced || this.lastSync == null || Date().time - (this.lastSync?.time ?: 0) > 180000) {
            lastSync = Date()
            return apiClient.retrieveUser(withTasks)
                    .doOnNext({ localRepository.saveUser(it) })
                    .doOnNext { user ->
                        if (withTasks) {
                            taskRepository.saveTasks(user.id, user.tasksOrder, user.tasks)
                        }
                    }
                    .flatMap { user ->
                        val calendar = GregorianCalendar()
                        val timeZone = calendar.timeZone
                        val offset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.timeInMillis).toLong(), TimeUnit.MILLISECONDS)
                        if (offset != user?.preferences?.timezoneOffset ?: 0) {
                            return@flatMap updateUser(user, "preferences.timezoneOffset", offset.toString())
                        } else {
                            return@flatMap Observable.just(user)
                        }
                    }
        } else {
            return user
        }
    }

    override fun getInboxMessages(replyToUserID: String?): Observable<RealmResults<ChatMessage>> =
            localRepository.getInboxMessages(userId, replyToUserID)

    override fun revive(user: User): Observable<User> =
            apiClient.revive().map { newUser -> mergeUser(user, newUser) }

    override fun resetTutorial(user: User?) {
        localRepository.tutorialSteps
                .map<Map<String, Any>> { tutorialSteps ->
                    val updateData = HashMap<String, Any>()
                    for (step in tutorialSteps) {
                        updateData.put("flags.tutorial." + step.tutorialGroup + "." + step.identifier, false)
                    }
                    updateData
                }
                .flatMap { updateData -> updateUser(user, updateData) }
                .subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }

    override fun sleep(user: User): Observable<User> {
        localRepository.executeTransaction { user.preferences.sleep = !user.preferences.sleep }
        return apiClient.sleep().map { user }
    }

    override fun getSkills(user: User): Observable<RealmResults<Skill>> =
            localRepository.getSkills(user)

    override fun getSpecialItems(user: User): Observable<RealmResults<Skill>> =
            localRepository.getSpecialItems(user)

    override fun useSkill(user: User?, key: String, target: String, taskId: String): Observable<SkillResponse> {
        return apiClient.useSkill(key, target, taskId).doOnNext { skillResponse ->
            if (user != null) {
                mergeUser(user, skillResponse.user)
            }
        }
    }

    override fun useSkill(user: User?, key: String, target: String): Observable<SkillResponse> {
        return apiClient.useSkill(key, target)
                .map { response ->
                    response.hpDiff = response.user.stats.getHp() - (user?.stats?.getHp() ?: 0.0)
                    response.expDiff = response.user.stats.getExp() - (user?.stats?.getExp() ?: 0.0)
                    response.goldDiff = response.user.stats.getGp() - (user?.stats?.getGp() ?: 0.0)
                    response
                }
                .doOnNext { skillResponse ->
                    if (user != null) {
                        mergeUser(user, skillResponse.user)
                    }
                }
    }

    override fun changeClass(): Observable<User> = apiClient.changeClass()

    override fun disableClasses(): Observable<User> = apiClient.disableClasses()

    override fun changeClass(selectedClass: String): Observable<User> = apiClient.changeClass(selectedClass)

    override fun unlockPath(user: User, customization: Customization): Observable<UnlockResponse> {
        return apiClient.unlockPath(customization.path)
                .doOnNext { unlockResponse ->
                    val copiedUser = localRepository.getUnmanagedCopy(user)
                    copiedUser.preferences = unlockResponse.preferences
                    copiedUser.purchased = unlockResponse.purchased
                    copiedUser.items = unlockResponse.items
                    copiedUser.balance = copiedUser.balance - customization.price / 4.0
                    localRepository.saveUser(copiedUser)
                }
    }

    override fun unlockPath(user: User, set: CustomizationSet): Observable<UnlockResponse> {
        var path = ""
        for (customization in set.customizations) {
            path = path + "," + customization.path
        }
        if (path.isEmpty()) {
            return Observable.just(null)
        }
        path = path.substring(1)
        return apiClient.unlockPath(path)
                .doOnNext { unlockResponse ->
                    val copiedUser = localRepository.getUnmanagedCopy(user)
                    copiedUser.preferences = unlockResponse.preferences
                    copiedUser.purchased = unlockResponse.purchased
                    copiedUser.items = unlockResponse.items
                    copiedUser.balance = copiedUser.balance - set.price / 4.0
                    localRepository.saveUser(copiedUser)
                }
    }

    override fun runCron() {
        runCron(ArrayList())
    }

    override fun readNotification(id: String): Observable<List<*>> = apiClient.readNotification(id)

    override fun changeCustomDayStart(dayStartTime: Int): Observable<User> {
        val updateObject = HashMap<String, Any>()
        updateObject.put("dayStart", dayStartTime)
        return apiClient.changeCustomDayStart(updateObject)
    }

    override fun updateLanguage(user: User?, languageCode: String): Observable<User> {
        return updateUser(user, "preferences.language", languageCode)
                .doOnNext { apiClient.setLanguageCode(languageCode) }
    }

    override fun resetAccount(): Observable<User> {
        return apiClient.resetAccount()
                .flatMap { retrieveUser(true, true) }
    }

    override fun deleteAccount(password: String): Observable<Void> =
            apiClient.deleteAccount(password)

    override fun sendPasswordResetEmail(email: String): Observable<Void> =
            apiClient.sendPasswordResetEmail(email)

    override fun updateLoginName(newLoginName: String, password: String): Observable<Void> =
            apiClient.updateLoginName(newLoginName, password)

    override fun updateEmail(newEmail: String, password: String): Observable<Void> =
            apiClient.updateEmail(newEmail, password)

    override fun updatePassword(newPassword: String, oldPassword: String, oldPasswordConfirmation: String): Observable<Void> =
            apiClient.updatePassword(newPassword, oldPassword, oldPasswordConfirmation)

    override fun allocatePoint(user: User?, stat: String): Observable<Stats> {
        if (user != null && user.isManaged) {
            localRepository.executeTransaction {
                when (stat) {
                    Stats.STRENGTH -> user.stats.str += 1
                    Stats.INTELLIGENCE -> user.stats._int += 1
                    Stats.CONSTITUTION -> user.stats.con += 1
                    Stats.PERCEPTION -> user.stats.per += 1
                }
                user.stats.points -= 1
            }
        }
        return apiClient.allocatePoint(stat)
                .doOnNext { stats ->
                    if (user != null && user.isManaged) {
                        localRepository.executeTransaction {
                            user.stats.str = stats.str
                            user.stats.con = stats.con
                            user.stats.per = stats.per
                            user.stats._int = stats._int
                            user.stats.points = stats.points
                            user.stats.mp = stats.mp
                        }
                    }
                }
    }

    override fun bulkAllocatePoints(user: User?, strength: Int, intelligence: Int, constitution: Int, perception: Int): Observable<Stats> =
            apiClient.bulkAllocatePoints(strength, intelligence, constitution, perception)
                    .doOnNext { stats ->
                        if (user != null && user.isManaged) {
                            localRepository.executeTransaction {
                                user.stats.str = stats.str
                                user.stats.con = stats.con
                                user.stats.per = stats.per
                                user.stats._int = stats._int
                                user.stats.points = stats.points
                                user.stats.mp = stats.mp
                            }
                        }
                    }

    override fun runCron(tasks: List<Task>) {
        val observable: Observable<List<TaskScoringResult?>> = if (tasks.isNotEmpty()) {
            Observable.from(tasks)
                    .flatMap { task -> taskRepository.taskChecked(null, task, true, true) }
                    .toList()
        } else {
            Observable.just(null)
        }
        localRepository.getUser(userId).first().subscribe(Action1 { user -> localRepository.executeTransaction { user.needsCron = false } }, RxErrorHandler.handleEmptyError())
        observable.flatMap { apiClient.runCron() }
                .flatMap { this.retrieveUser(true, true) }
                .subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }

    private fun mergeUser(oldUser: User?, newUser: User): User? {
        if (oldUser == null || !oldUser.isValid) {
            return oldUser
        }
        val copiedUser: User = if (oldUser.isManaged) {
            localRepository.getUnmanagedCopy(oldUser)
        } else {
            oldUser
        }
        if (newUser.items != null) {
            copiedUser.items = newUser.items
        }
        if (newUser.preferences != null) {
            copiedUser.preferences = newUser.preferences
        }
        if (newUser.flags != null) {
            copiedUser.flags = newUser.flags
        }
        if (newUser.stats != null) {
            copiedUser.stats.merge(newUser.stats)
        }

        localRepository.saveUser(copiedUser)
        return oldUser
    }
}
