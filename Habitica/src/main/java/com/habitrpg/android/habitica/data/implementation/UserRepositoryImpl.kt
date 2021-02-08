package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.data.local.UserQuestStatus
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.realm.RealmResults
import java.util.*
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(localRepository: UserLocalRepository, apiClient: ApiClient, userID: String, private val taskRepository: TaskRepository, var appConfigManager: AppConfigManager) : BaseRepositoryImpl<UserLocalRepository>(localRepository, apiClient, userID), UserRepository {

    private var lastSync: Date? = null

    override fun getUser(): Flowable<User> = getUser(userID)


    override fun getUser(userID: String): Flowable<User> = localRepository.getUser(userID)

    override fun updateUser(updateData: Map<String, Any>): Flowable<User> {
        return Flowable.zip(apiClient.updateUser(updateData),
                localRepository.getUser(userID).firstElement().toFlowable(),
                { newUser, user -> mergeUser(user, newUser) })
    }

    override fun updateUser(key: String, value: Any): Flowable<User> {
        val updateData = HashMap<String, Any>()
        updateData[key] = value
        return updateUser(updateData)
    }

    override fun retrieveUser(withTasks: Boolean): Flowable<User> =
            retrieveUser(withTasks, false)

    @Suppress("ReturnCount")
    override fun retrieveUser(withTasks: Boolean, forced: Boolean, overrideExisting: Boolean): Flowable<User> {
        // Only retrieve again after 3 minutes or it's forced.
        if (forced || this.lastSync == null || Date().time - (this.lastSync?.time ?: 0) > 180000) {
            lastSync = Date()
            return apiClient.retrieveUser(withTasks)
                    .doOnNext { localRepository.saveUser(it, overrideExisting) }
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
                            return@flatMap updateUser("preferences.timezoneOffset", offset.toString())
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
                    .flatMap { retrieveUser(false, true) }

    override fun resetTutorial() {
        localRepository.getTutorialSteps()
                .firstElement()
                .map<Map<String, Any>> { tutorialSteps ->
                    val updateData = HashMap<String, Any>()
                    for (step in tutorialSteps) {
                        updateData["flags.tutorial." + step.tutorialGroup + "." + step.identifier] = false
                    }
                    updateData
                }
                .flatMap { updateData -> updateUser(updateData).firstElement() }
                .subscribe({ }, RxErrorHandler.handleEmptyError())
    }

    override fun sleep(user: User): Flowable<User> {
        localRepository.modify(user) { it.preferences?.sleep = !(it.preferences?.sleep ?: false) }
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

    override fun unlockPath(user: User?, customization: Customization): Flowable<UnlockResponse> {
        var path = customization.path
        if (path.last() == '.' && customization.type == "background") {
            path += user?.preferences?.background
        }
        return Flowable.zip(apiClient.unlockPath(path), localRepository.getUser(userID).firstElement().toFlowable(), { unlockResponse, copiedUser ->
                    copiedUser.preferences = unlockResponse.preferences
                    copiedUser.purchased = unlockResponse.purchased
                    copiedUser.items = unlockResponse.items
                    copiedUser.balance = copiedUser.balance - (customization.price ?: 0) / 4.0
                    localRepository.saveUser(copiedUser, false)
            unlockResponse
                })
    }

    override fun unlockPath(set: CustomizationSet): Flowable<UnlockResponse> {
        var path = ""
        for (customization in set.customizations) {
            path = path + "," + customization.path
        }
        if (path.isEmpty()) {
            return Flowable.just(null)
        }
        path = path.substring(1)
        return Flowable.zip(apiClient.unlockPath(path), localRepository.getUser(userID).firstElement().toFlowable(), { unlockResponse, copiedUser ->
                    copiedUser.preferences = unlockResponse.preferences
                    copiedUser.purchased = unlockResponse.purchased
                    copiedUser.items = unlockResponse.items
                    copiedUser.balance = copiedUser.balance - set.price / 4.0
                    localRepository.saveUser(copiedUser, false)
            unlockResponse
                })
    }

    override fun runCron() {
        runCron(ArrayList())
    }

    override fun readNotification(id: String): Flowable<List<Any>> = apiClient.readNotification(id)
    override fun getUserQuestStatus(): Flowable<UserQuestStatus> {
        return localRepository.getUserQuestStatus(userID)
    }

    override fun reroll(): Flowable<User> {
        return apiClient.reroll()
                .flatMap { retrieveUser(true, true, true) }
    }

    override fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>> =
            apiClient.readNotifications(notificationIds)

    override fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<Any>> =
            apiClient.seeNotifications(notificationIds)

    override fun changeCustomDayStart(dayStartTime: Int): Flowable<User> {
        val updateObject = HashMap<String, Any>()
        updateObject["dayStart"] = dayStartTime
        return apiClient.changeCustomDayStart(updateObject)
    }

    override fun updateLanguage(languageCode: String): Flowable<User> {
        return updateUser("preferences.language", languageCode)
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
                    localRepository.modify(user) { liveUser ->
                        liveUser.authentication?.localAuthentication?.username = newLoginName
                        liveUser.flags?.verifiedUsername = true
                    }
                }
                .firstElement()
    }

    override fun verifyUsername(username: String): Flowable<VerifyUsernameResponse> = apiClient.verifyUsername(username.trim())

    override fun updateEmail(newEmail: String, password: String): Flowable<Void> =
            apiClient.updateEmail(newEmail.trim(), password)

    override fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Flowable<Void> =
            apiClient.updatePassword(oldPassword.trim(), newPassword.trim(), newPasswordConfirmation.trim())

    override fun allocatePoint(stat: String): Flowable<Stats> {
        localRepository.getUser(userID).subscribe( { liveUser ->
            when (stat) {
                Stats.STRENGTH -> liveUser.stats?.strength = liveUser.stats?.strength?.inc()
                Stats.INTELLIGENCE -> liveUser.stats?.intelligence = liveUser.stats?.intelligence?.inc()
                Stats.CONSTITUTION -> liveUser.stats?.constitution= liveUser.stats?.constitution?.inc()
                Stats.PERCEPTION -> liveUser.stats?.per = liveUser.stats?.per?.inc()
            }
            liveUser.stats?.points = liveUser.stats?.points?.dec()
        }, RxErrorHandler.handleEmptyError())
        return apiClient.allocatePoint(stat)
                .doOnNext { stats ->
                        /*localRepository.modify(user) { liveUser ->
                            liveUser.stats?.strength = stats.strength
                            liveUser.stats?.constitution = stats.constitution
                            liveUser.stats?.per = stats.per
                            liveUser.stats?.intelligence = stats.intelligence
                            liveUser.stats?.points = stats.points
                            liveUser.stats?.mp = stats.mp
                        }*/
                }
    }

    override fun bulkAllocatePoints(user: User?, strength: Int, intelligence: Int, constitution: Int, perception: Int): Flowable<Stats> =
            apiClient.bulkAllocatePoints(strength, intelligence, constitution, perception)
                    .doOnNext { stats ->
                        if (user != null && user.isManaged) {
                            localRepository.modify(user) { liveUser ->
                                liveUser.stats?.strength = stats.strength
                                liveUser.stats?.constitution = stats.constitution
                                liveUser.stats?.per = stats.per
                                liveUser.stats?.intelligence = stats.intelligence
                                liveUser.stats?.points = stats.points
                                liveUser.stats?.mp = stats.mp
                            }
                        }
                    }

    override fun runCron(tasks: MutableList<Task>) {
        var observable: Maybe<Any> = localRepository.getUser(userID).firstElement()
                .filter { it.needsCron }
                .map {  user ->
                    localRepository.modify(user) { liveUser ->
                        liveUser.needsCron = false
                        liveUser.lastCron = Date()
                    }
                    user
                }
        if (tasks.isNotEmpty()) {
            val scoringList = mutableListOf<Map<String, String>>()
            for (task in tasks) {
                val map = mutableMapOf<String, String>()
                map["id"] = task.id ?: ""
                map["direction"] = TaskDirection.UP.text
                scoringList.add(map)
            }
            observable = observable.flatMap { taskRepository.bulkScoreTasks(scoringList).firstElement() }
        }
        observable.flatMap { apiClient.runCron().firstElement() }
                .flatMap { this.retrieveUser(withTasks = true, forced = true).firstElement() }
                .subscribe({ }, RxErrorHandler.handleEmptyError())
    }

    override fun useCustomization(user: User?, type: String, category: String?, identifier: String): Flowable<User> {
        if (user != null && appConfigManager.enableLocalChanges()) {
            localRepository.modify(user) { liveUser ->
                when (type) {
                    "skin" -> liveUser.preferences?.skin =identifier
                    "shirt" -> liveUser.preferences?.shirt = identifier
                    "hair" -> {
                        when (category) {
                            "color" -> liveUser.preferences?.hair?.color = identifier
                            "flower" -> liveUser.preferences?.hair?.flower = identifier.toInt()
                            "mustache" -> liveUser.preferences?.hair?.mustache = identifier.toInt()
                            "beard" -> liveUser.preferences?.hair?.beard = identifier.toInt()
                            "bangs" -> liveUser.preferences?.hair?.bangs = identifier.toInt()
                            "base" -> liveUser.preferences?.hair?.base = identifier.toInt()
                        }
                    }
                    "background" -> liveUser.preferences?.background = identifier
                    "chair" -> liveUser.preferences?.chair = identifier
                }
            }
        }
        var updatePath = "preferences.$type"
        if (category != null) {
            updatePath = "$updatePath.$category"
        }
        return updateUser(updatePath, identifier)
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

    override fun retrieveTeamPlans(): Flowable<List<TeamPlan>> {
        return apiClient.getTeamPlans().doOnNext { teams ->
            teams.forEach { it.userID = userID }
            localRepository.save(teams)
        }
    }

    override fun getTeamPlans(): Flowable<RealmResults<TeamPlan>> {
        return localRepository.getTeamPlans(userID)
    }

    override fun retrieveTeamPlan(teamID: String): Flowable<Group> {
        return Flowable.zip(apiClient.getGroup(teamID), apiClient.getTeamPlanTasks(teamID),
                { team, tasks ->
                    team.tasks = tasks
                    team
                })
                .doOnNext { localRepository.save(it) }
                .doOnNext { team ->
                    val id = team.id
                    val tasksOrder = team.tasksOrder
                    val tasks = team.tasks
                    if (id.isNotBlank() && tasksOrder != null && tasks != null) {
                        taskRepository.saveTasks(id, tasksOrder, tasks)
                    }
                }
    }

    override fun getTeamPlan(teamID: String): Flowable<Group> {
        return localRepository.getTeamPlan(teamID)
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
        if (newUser.inbox != null) {
            copiedUser.inbox = newUser.inbox
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
        if (newUser.party != null) {
            copiedUser.party = newUser.party
        }
        copiedUser.needsCron = newUser.needsCron
        copiedUser.versionNumber = newUser.versionNumber

        localRepository.saveUser(copiedUser, false)
        return copiedUser
    }
}
