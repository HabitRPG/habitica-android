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
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(localRepository: UserLocalRepository, apiClient: ApiClient, private val userId: String, private val taskRepository: TaskRepository) : BaseRepositoryImpl<UserLocalRepository>(localRepository, apiClient), UserRepository {

    private var lastSync: Date? = null

    override fun getUser(): Flowable<User> = getUser(userId)
    override fun getInboxOverviewList(): Flowable<RealmResults<ChatMessage>> = localRepository.getInboxOverviewList(userId)


    override fun getUser(userID: String): Flowable<User> = localRepository.getUser(userID)

    override fun updateUser(user: User?, updateData: Map<String, Any>): Flowable<User> {
        return if (user == null) {
            Flowable.just(User())
        } else apiClient.updateUser(updateData).map { newUser -> mergeUser(user, newUser) }
    }

    override fun updateUser(user: User?, key: String, value: Any): Flowable<User> {
        val updateData = HashMap<String, Any>()
        updateData[key] = value
        return updateUser(user, updateData)
    }

    override fun retrieveUser(withTasks: Boolean): Flowable<User> =
            retrieveUser(withTasks, false)

    @Suppress("ReturnCount")
    override fun retrieveUser(withTasks: Boolean, forced: Boolean): Flowable<User> {
        if (forced || this.lastSync == null || Date().time - (this.lastSync?.time ?: 0) > 180000) {
            lastSync = Date()
            return apiClient.retrieveUser(withTasks)
                    .doOnNext { localRepository.saveUser(it) }
                    .doOnNext { user ->
                        if (withTasks) {
                            val id = user.id
                            val tasksOrder = user.tasksOrder
                            val tasks = user.tasks
                            if (id != null && tasksOrder != null && tasks != null) {
                                taskRepository.saveTasks(id, tasksOrder, tasks)
                            }
                        }
                    }
                    .flatMap { user ->
                        val calendar = GregorianCalendar()
                        val timeZone = calendar.timeZone
                        val offset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.timeInMillis).toLong(), TimeUnit.MILLISECONDS)
                        if (offset != user.preferences?.timezoneOffset ?: 0) {
                            return@flatMap updateUser(user, "preferences.timezoneOffset", offset.toString())
                        } else {
                            return@flatMap Flowable.just(user)
                        }
                    }
        } else {
            return getUser().take(1)
        }
    }

    override fun getInboxMessages(replyToUserID: String?): Flowable<RealmResults<ChatMessage>> =
            localRepository.getInboxMessages(userId, replyToUserID)

    override fun retrieveInboxMessages(): Flowable<List<ChatMessage>> {
        return apiClient.retrieveInboxMessages().doOnNext { messages ->
            messages.forEach {
                it.isInboxMessage = true
            }
            localRepository.save(messages)
        }
    }

    override fun revive(user: User): Flowable<User> =
            apiClient.revive().map { newUser -> mergeUser(user, newUser) }

    override fun resetTutorial(user: User?) {
        localRepository.getTutorialSteps()
                .map<Map<String, Any>> { tutorialSteps ->
                    val updateData = HashMap<String, Any>()
                    for (step in tutorialSteps) {
                        updateData["flags.tutorial." + step.tutorialGroup + "." + step.identifier] = false
                    }
                    updateData
                }
                .flatMap { updateData -> updateUser(user, updateData) }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    override fun sleep(user: User): Flowable<User> {
        localRepository.executeTransaction { user.preferences?.isSleep = !(user.preferences?.sleep ?: false) }
        return apiClient.sleep().map { user }
    }

    override fun getSkills(user: User): Flowable<RealmResults<Skill>> =
            localRepository.getSkills(user)

    override fun getSpecialItems(user: User): Flowable<RealmResults<Skill>> =
            localRepository.getSpecialItems(user)

    override fun useSkill(user: User?, key: String, target: String, taskId: String): Flowable<SkillResponse> {
        return apiClient.useSkill(key, target, taskId).doOnNext { skillResponse ->
            if (user != null) {
                mergeUser(user, skillResponse.user)
            }
        }
    }

    override fun useSkill(user: User?, key: String, target: String): Flowable<SkillResponse> {
        return apiClient.useSkill(key, target)
                .map { response ->
                    response.hpDiff = response.user.stats?.hp ?: 0 - (user?.stats?.hp ?: 0.0)
                    response.expDiff = response.user.stats?.exp ?: 0 - (user?.stats?.exp ?: 0.0)
                    response.goldDiff = response.user.stats?.gp ?: 0 - (user?.stats?.gp ?: 0.0)
                    response
                }
                .doOnNext { skillResponse ->
                    if (user != null) {
                        mergeUser(user, skillResponse.user)
                    }
                }
    }

    override fun changeClass(): Flowable<User> = apiClient.changeClass().flatMap { retrieveUser(false, true) }

    override fun disableClasses(): Flowable<User> = apiClient.disableClasses().flatMap { retrieveUser(false, true) }

    override fun changeClass(selectedClass: String): Flowable<User> = apiClient.changeClass(selectedClass)

    override fun unlockPath(user: User, customization: Customization): Flowable<UnlockResponse> {
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

    override fun unlockPath(user: User, set: CustomizationSet): Flowable<UnlockResponse> {
        var path = ""
        for (customization in set.customizations) {
            path = path + "," + customization.path
        }
        if (path.isEmpty()) {
            return Flowable.just(null)
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

    override fun readNotification(id: String): Flowable<List<*>> = apiClient.readNotification(id)

    override fun changeCustomDayStart(dayStartTime: Int): Flowable<User> {
        val updateObject = HashMap<String, Any>()
        updateObject["dayStart"] = dayStartTime
        return apiClient.changeCustomDayStart(updateObject)
    }

    override fun updateLanguage(user: User?, languageCode: String): Flowable<User> {
        return updateUser(user, "preferences.language", languageCode)
                .doOnNext { apiClient.setLanguageCode(languageCode) }
    }

    override fun resetAccount(): Flowable<User> {
        return apiClient.resetAccount().flatMap { retrieveUser(true, true) }
    }

    override fun deleteAccount(password: String): Flowable<Void> =
            apiClient.deleteAccount(password)

    override fun sendPasswordResetEmail(email: String): Flowable<Void> =
            apiClient.sendPasswordResetEmail(email)

    override fun updateLoginName(newLoginName: String, password: String): Flowable<Void> =
            apiClient.updateLoginName(newLoginName, password)

    override fun updateEmail(newEmail: String, password: String): Flowable<Void> =
            apiClient.updateEmail(newEmail, password)

    override fun updatePassword(newPassword: String, oldPassword: String, oldPasswordConfirmation: String): Flowable<Void> =
            apiClient.updatePassword(newPassword, oldPassword, oldPasswordConfirmation)

    override fun allocatePoint(user: User?, stat: String): Flowable<Stats> {
        if (user != null && user.isManaged) {
            localRepository.executeTransaction {
                when (stat) {
                    Stats.STRENGTH -> user.stats?.str = user.stats?.str?.inc()
                    Stats.INTELLIGENCE -> user.stats?._int = user.stats?._int?.inc()
                    Stats.CONSTITUTION -> user.stats?.con= user.stats?.con?.inc()
                    Stats.PERCEPTION -> user.stats?.per = user.stats?.per?.inc()
                }
                user.stats?.points = user.stats?.points?.dec()
            }
        }
        return apiClient.allocatePoint(stat)
                .doOnNext { stats ->
                    if (user != null && user.isManaged) {
                        localRepository.executeTransaction {
                            user.stats?.str = stats.str
                            user.stats?.con = stats.con
                            user.stats?.per = stats.per
                            user.stats?._int = stats._int
                            user.stats?.points = stats.points
                            user.stats?.mp = stats.mp
                        }
                    }
                }
    }

    override fun bulkAllocatePoints(user: User?, strength: Int, intelligence: Int, constitution: Int, perception: Int): Flowable<Stats> =
            apiClient.bulkAllocatePoints(strength, intelligence, constitution, perception)
                    .doOnNext { stats ->
                        if (user != null && user.isManaged) {
                            localRepository.executeTransaction {
                                user.stats?.str = stats.str
                                user.stats?.con = stats.con
                                user.stats?.per = stats.per
                                user.stats?._int = stats._int
                                user.stats?.points = stats.points
                                user.stats?.mp = stats.mp
                            }
                        }
                    }

    override fun runCron(tasks: MutableList<Task>) {
        var observable: Maybe<Any> = localRepository.getUser(userId).firstElement()
                .filter { it.needsCron }
                .map {  user -> localRepository.executeTransaction {
                    user.needsCron = false
                    user.lastCron = Date()
                }
                    user
                }
        if (tasks.isNotEmpty()) {
            for (task in tasks) {
                observable = observable.flatMap { taskRepository.taskChecked(null, task, true, true).firstElement() }
            }
        }
        observable.flatMap { apiClient.runCron().firstElement() }
                .flatMap { this.retrieveUser(true, true).firstElement() }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    private fun mergeUser(oldUser: User?, newUser: User): User {
        if (oldUser == null || !oldUser.isValid) {
            return oldUser ?: newUser
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
            copiedUser.stats?.merge(newUser.stats)
        }

        localRepository.saveUser(copiedUser)
        return oldUser
    }
}
