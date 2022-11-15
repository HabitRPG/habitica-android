package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.extensions.filterMapEmpty
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.common.habitica.extensions.Optional
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.shared.habitica.models.tasks.Attribute
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.functions.BiFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
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

    private suspend fun updateUser(userID: String, updateData: Map<String, Any>): User? {
        val networkUser = apiClient.updateUser(updateData) ?: return null
        val oldUser = localRepository.getUser(userID).firstOrNull()
        return mergeUser(oldUser, networkUser)
    }

    private suspend fun updateUser(userID: String, key: String, value: Any): User? {
        val updateData = HashMap<String, Any>()
        updateData[key] = value
        return updateUser(userID, updateData)
    }

    override suspend fun updateUser(updateData: Map<String, Any>): User? {
        return updateUser(userID, updateData)
    }

    override suspend fun updateUser(key: String, value: Any): User? {
        return updateUser(userID, key, value)
    }

    @Suppress("ReturnCount")
    override suspend fun retrieveUser(withTasks: Boolean, forced: Boolean, overrideExisting: Boolean): User? {
        // Only retrieve again after 3 minutes or it's forced.
        if (forced || this.lastSync == null || Date().time - (this.lastSync?.time ?: 0) > 180000) {
            val user = apiClient.retrieveUser(withTasks) ?: return null
            lastSync = Date()
            localRepository.saveUser(user)
            if (withTasks) {
                val id = user.id
                val tasksOrder = user.tasksOrder
                val tasks = user.tasks
                if (id != null && tasksOrder != null && tasks != null) {
                    taskRepository.saveTasks(id, tasksOrder, tasks)
                }
            }
            val calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val offset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.timeInMillis).toLong(), TimeUnit.MILLISECONDS)
            return if (offset.toInt() != (user.preferences?.timezoneOffset ?: 0)) {
                updateUser(user.id ?: "", "preferences.timezoneOffset", offset.toString())
            } else {
                user
            }
        } else {
            return null
        }
    }

    override suspend fun revive(): User? {
        apiClient.revive()
        return retrieveUser(false, true)
    }

    override suspend fun resetTutorial(): User? {
        val tutorialSteps = localRepository.getTutorialSteps().firstOrNull() ?: return null
        val updateData = HashMap<String, Any>()
        for (step in tutorialSteps) {
            updateData[step.flagPath] = false
        }
        return updateUser(updateData)
    }

    override suspend fun sleep(user: User): User {
        val newValue = !(user.preferences?.sleep ?: false)
        localRepository.modify(user) { it.preferences?.sleep = newValue }
        if (apiClient.sleep() != true) {
            localRepository.modify(user) { it.preferences?.sleep = !newValue }
        }
        return user
    }

    override fun getSkills(user: User): Flowable<out List<Skill>> =
        localRepository.getSkills(user)

    override fun getSpecialItems(user: User): Flowable<out List<Skill>> =
        localRepository.getSpecialItems(user)

    override suspend fun useSkill(key: String, target: String?, taskId: String): SkillResponse? {
        val response = apiClient.useSkill(key, target ?: "", taskId) ?: return null
        val user = getLiveUser() ?: return response
        response.hpDiff = (response.user?.stats?.hp ?: 0.0) - (user.stats?.hp ?: 0.0)
        response.expDiff =(response.user?.stats?.exp ?: 0.0) - (user.stats?.exp ?: 0.0)
        response.goldDiff = (response.user?.stats?.gp ?: 0.0) - (user.stats?.gp ?: 0.0)
        response.damage = (response.user?.party?.quest?.progress?.up ?: 0.0f) - (user.party?.quest?.progress?.up ?: 0.0f)
        response.user?.let { mergeUser(user, it) }
        return response
    }

    override suspend fun useSkill(key: String, target: String?): SkillResponse? {
        val response = apiClient.useSkill(key, target ?: "") ?: return null
        val user = getLiveUser() ?: return response
        response.hpDiff = (response.user?.stats?.hp ?: 0.0) - (user.stats?.hp ?: 0.0)
        response.expDiff =(response.user?.stats?.exp ?: 0.0) - (user.stats?.exp ?: 0.0)
        response.goldDiff = (response.user?.stats?.gp ?: 0.0) - (user.stats?.gp ?: 0.0)
        response.damage = (response.user?.party?.quest?.progress?.up ?: 0.0f) - (user.party?.quest?.progress?.up ?: 0.0f)
        response.user?.let { mergeUser(user, it) }
        return response
    }

    override suspend fun disableClasses(): User? = apiClient.disableClasses()

    override suspend fun changeClass(selectedClass: String?): User? {
        apiClient.changeClass(selectedClass)
        return retrieveUser(false, forced = true)
    }

    override suspend fun unlockPath(customization: Customization): UnlockResponse? {
        return unlockPath(customization.path, customization.price ?: 0)
    }

    override suspend fun unlockPath(path: String, price: Int): UnlockResponse? {
        val unlockResponse = apiClient.unlockPath(path) ?: return null
        val user = localRepository.getUser(userID).firstOrNull() ?: return unlockResponse
        user.preferences = unlockResponse.preferences
        user.purchased = unlockResponse.purchased
        user.items = unlockResponse.items
        user.balance = user.balance - (price / 4.0)
        localRepository.saveUser(user, false)
        return unlockResponse
    }

    override suspend fun runCron() {
        runCron(ArrayList())
    }

    override fun readNotification(id: String): Flowable<List<Any>> = apiClient.readNotification(id)
    override fun getUserQuestStatus(): Flowable<UserQuestStatus> {
        return localRepository.getUserQuestStatus(userID)
    }

    override suspend fun reroll(): User? {
        return apiClient.reroll()
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

    override suspend fun updateLanguage(languageCode: String): User? {
        val user = updateUser("preferences.language", languageCode)
        apiClient.setLanguageCode(languageCode)
        return user
    }

    override suspend fun resetAccount(): User? {
        apiClient.resetAccount()
        return retrieveUser(withTasks = true, forced = true)
    }

    override fun deleteAccount(password: String): Flowable<Void> =
        apiClient.deleteAccount(password)

    override fun sendPasswordResetEmail(email: String): Flowable<Void> =
        apiClient.sendPasswordResetEmail(email)

    override suspend fun updateLoginName(newLoginName: String, password: String?): User? {
        if (password != null && password.isNotEmpty()) {
            apiClient.updateLoginName(newLoginName.trim(), password.trim())
        } else {
            apiClient.updateUsername(newLoginName.trim())
        }
        val user = localRepository.getUser(userID).firstOrNull() ?: return null
        localRepository.modify(user) { liveUser ->
            liveUser.authentication?.localAuthentication?.username = newLoginName
            liveUser.flags?.verifiedUsername = true
        }
        return user
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
        getLiveUserFlowable().firstElement().subscribe(
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
            ExceptionHandler.rx()
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

    override suspend fun runCron(tasks: MutableList<Task>) {
        withContext(Dispatchers.Main) {
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
                // .flatMap {
                // this.retrieveUser(withTasks = true, forced = true)
                // }
                .subscribe({ }, ExceptionHandler.rx())
        }
    }

    override suspend fun useCustomization(type: String, category: String?, identifier: String): User? {
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
                ExceptionHandler.rx()
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

    override suspend fun retrieveTeamPlan(teamID: String): Group? {
        val team = apiClient.getGroup(teamID) ?: return null
        val tasks = apiClient.getTeamPlanTasks(teamID)
        localRepository.save(team)
        val id = team.id
        val tasksOrder = team.tasksOrder
        if (id.isNotBlank() && tasksOrder != null && tasks != null) {
            taskRepository.saveTasks(id, tasksOrder, tasks)
        }
        val members = apiClient.getGroupMembers(teamID, true) ?: return team
        localRepository.save(members.map { it.id?.let { member -> GroupMembership(member, id) } }.filterNotNull())
        members.let { localRepository.save(members) }
        return team
    }

    override fun getTeamPlan(teamID: String): Flowable<Group> {
        return localRepository.getTeamPlan(teamID)
    }

    private fun getLiveUserFlowable(): Flowable<User> {
        return localRepository.getUserFlowable(userID)
            .map { Optional(localRepository.getLiveObject(it)) }
            .filterMapEmpty()
    }

    private suspend fun getLiveUser(): User? {
        val user = localRepository.getUser(userID).firstOrNull() ?: return null
        return localRepository.getLiveObject(user)
    }

    private fun <T : Any> zipWithLiveUser(flowable: Flowable<T>, mergeFunc: BiFunction<T, User, T>): Flowable<T> {
        return Flowable.zip(flowable, getLiveUserFlowable().firstElement().toFlowable(), mergeFunc)
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
