package com.habitrpg.android.habitica.data.implementation

import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.data.local.UserQuestStatus
import com.habitrpg.common.habitica.extensions.Optional
import com.habitrpg.android.habitica.extensions.filterMapEmpty
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.common.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.common.habitica.models.tasks.Attribute
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.functions.BiFunction
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

class UserRepositoryImpl(
    localRepository: UserLocalRepository,
    apiClient: ApiClient,
    userID: String,
    private val taskRepository: TaskRepository,
    private val appConfigManager: AppConfigManager,
    private val analyticsManager: AnalyticsManager
) : BaseRepositoryImpl<UserLocalRepository>(localRepository, apiClient, userID), UserRepository {

    private var lastSync: Date? = null

    override fun getUser(): Flow<User?> = getUser(userID)
    override fun getUserFlowable(): Flowable<User> = localRepository.getUserFlowable(userID)

    override fun getUser(userID: String): Flow<User?> = localRepository.getUser(userID)

    private fun updateUser(userID: String, updateData: Map<String, Any>): Flowable<User> {
        return Flowable.zip(
            apiClient.updateUser(updateData),
            localRepository.getUserFlowable(userID).firstElement().toFlowable()
        ) { newUser, user -> mergeUser(user, newUser) }
    }

    private fun updateUser(userID: String, key: String, value: Any): Flowable<User> {
        val updateData = HashMap<String, Any>()
        updateData[key] = value
        return updateUser(userID, updateData)
    }

    override fun updateUser(updateData: Map<String, Any>): Flowable<User> {
        return updateUser(userID, updateData)
    }

    override fun updateUser(key: String, value: Any): Flowable<User> {
        return updateUser(userID, key, value)
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
                        return@flatMap updateUser(user.id ?: "", "preferences.timezoneOffset", offset.toString())
                    } else {
                        return@flatMap Flowable.just(user)
                    }
                }
        } else {
            return localRepository.getUserFlowable(userID).take(1)
        }
    }

    override fun revive(): Flowable<User> = zipWithLiveUser(apiClient.revive()) { newUser, user ->
        mergeUser(user, newUser)
    }
        .flatMap { retrieveUser(false, true) }

    override fun resetTutorial(): Maybe<User> {
        return localRepository.getTutorialSteps()
            .firstElement()
            .map<Map<String, Any>> { tutorialSteps ->
                val updateData = HashMap<String, Any>()
                for (step in tutorialSteps) {
                    updateData[step.flagPath] = false
                }
                updateData
            }
            .flatMap { updateData -> updateUser(updateData).firstElement() }
    }

    override fun sleep(user: User): Flowable<User> {
        localRepository.modify(user) { it.preferences?.sleep = !(it.preferences?.sleep ?: false) }
        return apiClient.sleep().map { user }
    }

    override fun getSkills(user: User): Flowable<out List<Skill>> =
        localRepository.getSkills(user)

    override fun getSpecialItems(user: User): Flowable<out List<Skill>> =
        localRepository.getSpecialItems(user)

    override fun useSkill(key: String, target: String?, taskId: String): Flowable<SkillResponse> {
        return zipWithLiveUser(apiClient.useSkill(key, target ?: "", taskId)) { response, user ->
            response.hpDiff = response.user?.stats?.hp ?: 0 - (user.stats?.hp ?: 0.0)
            response.expDiff = response.user?.stats?.exp ?: 0 - (user.stats?.exp ?: 0.0)
            response.goldDiff = response.user?.stats?.gp ?: 0 - (user.stats?.gp ?: 0.0)
            response.damage = (response.user?.party?.quest?.progress?.up ?: 0.0f) - (user.party?.quest?.progress?.up ?: 0.0f)
            response.user?.let { mergeUser(user, it) }
            response
        }
    }

    override fun useSkill(key: String, target: String?): Flowable<SkillResponse> {
        return zipWithLiveUser(apiClient.useSkill(key, target ?: "")) { response, user ->
            response.hpDiff = response.user?.stats?.hp ?: 0 - (user.stats?.hp ?: 0.0)
            response.expDiff = response.user?.stats?.exp ?: 0 - (user.stats?.exp ?: 0.0)
            response.goldDiff = response.user?.stats?.gp ?: 0 - (user.stats?.gp ?: 0.0)
            response.damage = (response.user?.party?.quest?.progress?.up ?: 0.0f) - (user.party?.quest?.progress?.up ?: 0.0f)
            response.user?.let { mergeUser(user, it) }
            response
        }
    }

    override fun changeClass(): Flowable<User> = apiClient.changeClass().flatMap { retrieveUser(withTasks = false, forced = true) }

    override fun disableClasses(): Flowable<User> = apiClient.disableClasses().flatMap { retrieveUser(withTasks = false, forced = true) }

    override fun changeClass(selectedClass: String): Flowable<User> = apiClient.changeClass(selectedClass)
        .flatMap { retrieveUser(false) }

    override fun unlockPath(path: String, price: Int): Flowable<UnlockResponse> {
        return zipWithLiveUser(apiClient.unlockPath(path)) { unlockResponse, copiedUser ->
            val user = localRepository.getUnmanagedCopy(copiedUser)
            user.preferences = unlockResponse.preferences
            user.purchased = unlockResponse.purchased
            user.items = unlockResponse.items
            user.balance = copiedUser.balance - (price / 4.0)
            localRepository.saveUser(copiedUser, false)
            unlockResponse
        }
    }

    override fun unlockPath(customization: Customization): Flowable<UnlockResponse> {
        return unlockPath(customization.path, customization.price ?: 0)
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
        return (
            if (password != null && password.isNotEmpty()) {
                apiClient.updateLoginName(newLoginName.trim(), password.trim())
            } else {
                apiClient.updateUsername(newLoginName.trim())
            }
            ).flatMapMaybe { localRepository.getUserFlowable(userID).firstElement() }
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

    override fun updatePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): Flowable<Void> =
        apiClient.updatePassword(oldPassword.trim(), newPassword.trim(), newPasswordConfirmation.trim())

    override fun allocatePoint(stat: Attribute): Flowable<Stats> {
        getLiveUser().firstElement().subscribe(
            { liveUser ->
                localRepository.executeTransaction {
                    when (stat) {
                        Attribute.STRENGTH -> liveUser.stats?.strength = liveUser.stats?.strength?.inc()
                        Attribute.INTELLIGENCE -> liveUser.stats?.intelligence = liveUser.stats?.intelligence?.inc()
                        Attribute.CONSTITUTION -> liveUser.stats?.constitution = liveUser.stats?.constitution?.inc()
                        Attribute.PERCEPTION -> liveUser.stats?.per = liveUser.stats?.per?.inc()
                    }
                    liveUser.stats?.points = liveUser.stats?.points?.dec()
                }
            },
            RxErrorHandler.handleEmptyError()
        )
        return zipWithLiveUser(apiClient.allocatePoint(stat.value)) { stats, user ->
            localRepository.modify(user) { liveUser ->
                liveUser.stats?.strength = stats.strength
                liveUser.stats?.constitution = stats.constitution
                liveUser.stats?.per = stats.per
                liveUser.stats?.intelligence = stats.intelligence
                liveUser.stats?.points = stats.points
                liveUser.stats?.mp = stats.mp
            }
            stats
        }
    }

    override fun bulkAllocatePoints(
        strength: Int,
        intelligence: Int,
        constitution: Int,
        perception: Int
    ): Flowable<Stats> =
        zipWithLiveUser(apiClient.bulkAllocatePoints(strength, intelligence, constitution, perception)) { stats, user ->
            localRepository.modify(user) { liveUser ->
                liveUser.stats?.strength = stats.strength
                liveUser.stats?.constitution = stats.constitution
                liveUser.stats?.per = stats.per
                liveUser.stats?.intelligence = stats.intelligence
                liveUser.stats?.points = stats.points
                liveUser.stats?.mp = stats.mp
            }
            stats
        }

    override fun runCron(tasks: MutableList<Task>) {
        var observable: Maybe<Any> = localRepository.getUserFlowable(userID).firstElement()
            .filter { it.needsCron }
            .map { user ->
                localRepository.modify(user) { liveUser ->
                    liveUser.needsCron = false
                    liveUser.lastCron = Date()
                }
                user
            }
        if (tasks.isNotEmpty()) {
            val scoringList = tasks.map { mapOf(Pair("id", it.id ?: ""), Pair("direction", TaskDirection.UP.text)) }
            observable = observable.flatMap { taskRepository.bulkScoreTasks(scoringList).firstElement() }
        }
        observable.flatMap { apiClient.runCron().firstElement() }
            .flatMap { this.retrieveUser(withTasks = true, forced = true).firstElement() }
            .subscribe({ }, {
                analyticsManager.logEvent("cron failed", bundleOf(Pair("error", it.localizedMessage)))
                RxErrorHandler.reportError(it)
            })
    }

    override fun useCustomization(type: String, category: String?, identifier: String): Flowable<User> {
        if (appConfigManager.enableLocalChanges()) {
            localRepository.getUserFlowable(userID).firstElement().subscribe(
                { liveUser ->
                    localRepository.modify(liveUser) { user ->
                        when (type) {
                            "skin" -> user.preferences?.skin = identifier
                            "shirt" -> user.preferences?.shirt = identifier
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
                            "background" -> user.preferences?.background = identifier
                            "chair" -> user.preferences?.chair = identifier
                        }
                    }
                },
                RxErrorHandler.handleEmptyError()
            )
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

    override fun getAchievements(): Flow<List<Achievement>> {
        return localRepository.getAchievements()
    }

    override fun getQuestAchievements(): Flow<List<QuestAchievement>> {
        return localRepository.getQuestAchievements(userID)
    }

    override fun retrieveTeamPlans(): Flowable<List<TeamPlan>> {
        return apiClient.getTeamPlans().doOnNext { teams ->
            teams.forEach { it.userID = userID }
            localRepository.save(teams)
        }
    }

    override fun getTeamPlans(): Flow<List<TeamPlan>> {
        return localRepository.getTeamPlans(userID)
    }

    override fun retrieveTeamPlan(teamID: String): Flowable<Group> {
        return Flowable.zip(
            apiClient.getGroup(teamID), apiClient.getTeamPlanTasks(teamID)
        ) { team, tasks ->
            team.tasks = tasks
            team
        }
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

    private fun getLiveUser(): Flowable<User> {
        return localRepository.getUserFlowable(userID)
            .map { Optional(localRepository.getLiveObject(it)) }
            .filterMapEmpty()
    }

    private fun <T : Any> zipWithLiveUser(flowable: Flowable<T>, mergeFunc: BiFunction<T, User, T>): Flowable<T> {
        return Flowable.zip(flowable, getLiveUser().firstElement().toFlowable(), mergeFunc)
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
