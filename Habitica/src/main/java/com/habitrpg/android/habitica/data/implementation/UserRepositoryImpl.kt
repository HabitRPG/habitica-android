package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(localRepository: UserLocalRepository, apiClient: ApiClient, userID: String, private val taskRepository: TaskRepository, var appConfigManager: AppConfigManager) : BaseRepositoryImpl<UserLocalRepository>(localRepository, apiClient, userID), UserRepository {

    private var lastSync: Date? = null

    override fun getUser(): Flowable<User> = getUser(userID)


    override fun getUser(userID: String): Flowable<User> = localRepository.getUser(userID)

    override fun updateUser(user: User?, updateData: Map<String, Any>): Flowable<User> {
        return apiClient.updateUser(updateData).map { newUser -> mergeUser(user, newUser) }
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
                        if (offset.toInt() != user.preferences?.timezoneOffset ?: 0) {
                            return@flatMap updateUser(user, "preferences.timezoneOffset", offset.toString())
                        } else {
                            return@flatMap Flowable.just(user)
                        }
                    }
        } else {
            return getUser().take(1)
        }
    }

    override fun revive(user: User): Flowable<User> =
            apiClient.revive().map { newUser -> mergeUser(user, newUser) }

    override fun resetTutorial(user: User?) {
        localRepository.getTutorialSteps()
                .firstElement()
                .map<Map<String, Any>> { tutorialSteps ->
                    val updateData = HashMap<String, Any>()
                    for (step in tutorialSteps) {
                        updateData["flags.tutorial." + step.tutorialGroup + "." + step.identifier] = false
                    }
                    updateData
                }
                .flatMap { updateData -> updateUser(user, updateData).firstElement() }
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

    override fun useSkill(user: User?, key: String, target: String?, taskId: String): Flowable<SkillResponse> {
        return apiClient.useSkill(key, target ?: "", taskId).doOnNext { skillResponse ->
            if (user != null) {
                mergeUser(user, skillResponse.user)
            }
        }
    }

    override fun useSkill(user: User?, key: String, target: String?): Flowable<SkillResponse> {
        return apiClient.useSkill(key, target ?: "")
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

    override fun changeClass(): Flowable<User> = apiClient.changeClass().flatMap { retrieveUser(withTasks = false, forced = true) }

    override fun disableClasses(): Flowable<User> = apiClient.disableClasses().flatMap { retrieveUser(withTasks = false, forced = true) }

    override fun changeClass(selectedClass: String): Flowable<User> = apiClient.changeClass(selectedClass).flatMap { retrieveUser(false) }

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
    override fun getIsUserOnQuest(): Flowable<Boolean> {
        return localRepository.getIsUserOnQuest(userID)
    }

    override fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<*>> =
            apiClient.readNotifications(notificationIds)

    override fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<*>> =
            apiClient.seeNotifications(notificationIds)

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
        return apiClient.resetAccount().flatMap { retrieveUser(withTasks = true, forced = true) }
    }

    override fun deleteAccount(password: String): Flowable<Void> =
            apiClient.deleteAccount(password)

    override fun sendPasswordResetEmail(email: String): Flowable<Void> =
            apiClient.sendPasswordResetEmail(email)

    override fun updateLoginName(newLoginName: String, password: String?): Maybe<User> {
        return (if (password != null && password.isNotEmpty()) {
            apiClient.updateLoginName(newLoginName.trim(), password.trim())
        } else {
            apiClient.updateUsername(newLoginName.trim())
        }).flatMapMaybe { localRepository.getUser(userID).firstElement() }
                .doOnNext { user ->
                    localRepository.executeTransaction {
                        user.authentication?.localAuthentication?.username = newLoginName
                        user.flags?.isVerifiedUsername = true
                    }
                }
                .firstElement()
    }

    override fun verifyUsername(username: String): Flowable<VerifyUsernameResponse> = apiClient.verifyUsername(username.trim())

    override fun updateEmail(newEmail: String, password: String): Flowable<Void> =
            apiClient.updateEmail(newEmail.trim(), password)

    override fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Flowable<Void> =
            apiClient.updatePassword(oldPassword.trim(), newPassword.trim(), newPasswordConfirmation.trim())

    override fun allocatePoint(user: User?, stat: String): Flowable<Stats> {
        if (user != null && user.isManaged) {
            localRepository.executeTransaction {
                when (stat) {
                    Stats.STRENGTH -> user.stats?.strength = user.stats?.strength?.inc()
                    Stats.INTELLIGENCE -> user.stats?.intelligence = user.stats?.intelligence?.inc()
                    Stats.CONSTITUTION -> user.stats?.constitution= user.stats?.constitution?.inc()
                    Stats.PERCEPTION -> user.stats?.per = user.stats?.per?.inc()
                }
                user.stats?.points = user.stats?.points?.dec()
            }
        }
        return apiClient.allocatePoint(stat)
                .doOnNext { stats ->
                    if (user != null && user.isManaged) {
                        localRepository.executeTransaction {
                            user.stats?.strength = stats.strength
                            user.stats?.constitution = stats.constitution
                            user.stats?.per = stats.per
                            user.stats?.intelligence = stats.intelligence
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
                                user.stats?.strength = stats.strength
                                user.stats?.constitution = stats.constitution
                                user.stats?.per = stats.per
                                user.stats?.intelligence = stats.intelligence
                                user.stats?.points = stats.points
                                user.stats?.mp = stats.mp
                            }
                        }
                    }

    override fun runCron(tasks: MutableList<Task>) {
        var observable: Maybe<Any> = localRepository.getUser(userID).firstElement()
                .filter { it.needsCron }
                .map {  user -> localRepository.executeTransaction {
                    user.needsCron = false
                    user.lastCron = Date()
                }
                    user
                }
        if (tasks.isNotEmpty()) {
            for (task in tasks) {
                observable = observable.flatMap { taskRepository.taskChecked(null, task, true, true, null).firstElement() }
            }
        }
        observable.flatMap { apiClient.runCron().firstElement() }
                .flatMap { this.retrieveUser(withTasks = true, forced = true).firstElement() }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    override fun useCustomization(user: User?, type: String, category: String?, identifier: String): Flowable<User> {
        if (user != null && appConfigManager.enableLocalChanges()) {
            localRepository.executeTransaction {
                when (type) {
                    "skin" -> user.preferences?.setSkin(identifier)
                    "shirt" -> user.preferences?.setShirt(identifier)
                    "hair" -> {
                        when (category) {
                            "color" -> user.preferences?.hair?.color = identifier
                            "flower" -> user.preferences?.hair?.flower = identifier.toInt()
                            "mustache" -> user.preferences?.hair?.mustache = identifier.toInt()
                            "beard" -> user.preferences?.hair?.beard = identifier.toInt()
                            "bangs" -> user.preferences?.hair?.bangs = identifier.toInt()
                            "base" -> user.preferences?.hair?.base = identifier.toInt()
                        }
                    }
                    "background" -> user.preferences?.setBackground(identifier)
                    "chair" -> user.preferences?.setChair(identifier)
                }
            }
        }
        var updatePath = "preferences.$type"
        if (category != null) {
            updatePath = "$updatePath.$category"
        }
        return updateUser(user, updatePath, identifier)
    }

    override fun retrieveAchievements(): Flowable<List<Achievement>> {
        return apiClient.getMemberAchievements(userID).doOnNext {
            localRepository.save(it)
        }
    }

    override fun getAchievements(): Flowable<RealmResults<Achievement>> {
        return localRepository.getAchievements()
    }

    override fun getQuestAchievements(): Flowable<RealmResults<QuestAchievement>> {
        return localRepository.getQuestAchievements(userID)
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
        if (newUser.profile != null) {
            copiedUser.profile = newUser.profile
        }
        copiedUser.versionNumber = newUser.versionNumber

        localRepository.saveUser(copiedUser)
        return copiedUser
    }
}
