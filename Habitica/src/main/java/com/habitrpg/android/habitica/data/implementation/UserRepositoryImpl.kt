package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
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
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.Attribute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImpl(
    localRepository: UserLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler,
    private val taskRepository: TaskRepository,
    private val appConfigManager: AppConfigManager,
    private val analyticsManager: AnalyticsManager
) : BaseRepositoryImpl<UserLocalRepository>(localRepository, apiClient, authenticationHandler), UserRepository {

    companion object {
        private var lastReadNotification: String? = null
        private var lastSync: Date? = null
    }

    override fun getUser(): Flow<User?> = authenticationHandler.userIDFlow.flatMapLatest { getUser(it) }
    override fun getUser(userID: String): Flow<User?> = localRepository.getUser(userID)

    private suspend fun updateUser(userID: String, updateData: Map<String, Any?>): User? {
        val networkUser = apiClient.updateUser(updateData) ?: return null
        val oldUser = localRepository.getUser(userID).firstOrNull()
        return mergeUser(oldUser, networkUser)
    }

    private suspend fun updateUser(userID : String, key : String, value : Any?): User? {
        return updateUser(userID, mapOf(key to value))
    }

    override suspend fun updateUser(updateData: Map<String, Any?>): User? {
        return updateUser(currentUserID, updateData)
    }

    override suspend fun updateUser(key : String, value : Any?): User? {
        return updateUser(currentUserID, key, value)
    }

    @Suppress("ReturnCount")
    override suspend fun retrieveUser(withTasks: Boolean, forced: Boolean, overrideExisting: Boolean): User? {
        // Only retrieve again after 3 minutes or it's forced.
        if (forced || lastSync == null || Date().time - (lastSync?.time ?: 0) > 180000) {
            val user = apiClient.retrieveUser(withTasks) ?: return null
            lastSync = Date()
            withContext(Dispatchers.Main) {
                localRepository.saveUser(user)
            }
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
        if (apiClient.sleep() == null) {
            localRepository.modify(user) { it.preferences?.sleep = !newValue }
        }
        return user
    }

    override fun getSkills(user: User) = localRepository.getSkills(user)

    override fun getSpecialItems(user: User) = localRepository.getSpecialItems(user)

    override suspend fun useSkill(key: String, target: String?, taskId: String): SkillResponse? {
        val response = apiClient.useSkill(key, target ?: "", taskId) ?: return null
        val user = getLiveUser() ?: return response
        response.hpDiff = (response.user?.stats?.hp ?: 0.0) - (user.stats?.hp ?: 0.0)
        response.expDiff = (response.user?.stats?.exp ?: 0.0) - (user.stats?.exp ?: 0.0)
        response.goldDiff = (response.user?.stats?.gp ?: 0.0) - (user.stats?.gp ?: 0.0)
        response.damage = (response.user?.party?.quest?.progress?.up ?: 0.0f) - (user.party?.quest?.progress?.up ?: 0.0f)
        response.user?.let { mergeUser(user, it) }
        return response
    }

    override suspend fun useSkill(key: String, target: String?): SkillResponse? {
        val response = apiClient.useSkill(key, target ?: "") ?: return null
        val user = getLiveUser() ?: return response
        response.hpDiff = (response.user?.stats?.hp ?: 0.0) - (user.stats?.hp ?: 0.0)
        response.expDiff = (response.user?.stats?.exp ?: 0.0) - (user.stats?.exp ?: 0.0)
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
        val user = localRepository.getUser(currentUserID).firstOrNull() ?: return unlockResponse
        localRepository.modify(user) { liveUser ->
            unlockResponse.preferences?.let { liveUser.preferences = it }
            liveUser.purchased = unlockResponse.purchased
            liveUser.items = unlockResponse.items
            liveUser.balance = liveUser.balance - (price / 4.0)
        }
        return unlockResponse
    }

    override suspend fun runCron() {
        runCron(ArrayList())
    }

    override suspend fun readNotification(id: String): List<Any>? {
        if (lastReadNotification == id) return null
        lastReadNotification = id
        return apiClient.readNotification(id)
    }
    override fun getUserQuestStatus(): Flow<UserQuestStatus> {
        return localRepository.getUserQuestStatus(currentUserID)
    }

    override suspend fun reroll(): User? {
        return apiClient.reroll()
    }

    override suspend fun readNotifications(notificationIds: Map<String, List<String>>) = apiClient.readNotifications(notificationIds)

    override suspend fun seeNotifications(notificationIds: Map<String, List<String>>) = apiClient.seeNotifications(notificationIds)

    override suspend fun changeCustomDayStart(dayStartTime: Int): User? {
        val updateObject = HashMap<String, Any>()
        updateObject["dayStart"] = dayStartTime
        return apiClient.changeCustomDayStart(updateObject)
    }

    override suspend fun updateLanguage(languageCode: String): User? {
        val user = updateUser("preferences.language", languageCode)
        apiClient.languageCode = languageCode
        return user
    }

    override suspend fun resetAccount(): User? {
        apiClient.resetAccount()
        return retrieveUser(withTasks = true, forced = true)
    }

    override suspend fun deleteAccount(password: String) = apiClient.deleteAccount(password)

    override suspend fun sendPasswordResetEmail(email: String) = apiClient.sendPasswordResetEmail(email)

    override suspend fun updateLoginName(newLoginName: String, password: String?): User? {
        if (!password.isNullOrEmpty()) {
            apiClient.updateLoginName(newLoginName.trim(), password.trim())
        } else {
            apiClient.updateUsername(newLoginName.trim())
        }
        val user = localRepository.getUser(currentUserID).firstOrNull() ?: return null
        localRepository.modify(user) { liveUser ->
            liveUser.authentication?.localAuthentication?.username = newLoginName
            liveUser.flags?.verifiedUsername = true
        }
        return user
    }

    override suspend fun verifyUsername(username: String) = apiClient.verifyUsername(username.trim())

    override suspend fun updateEmail(newEmail: String, password: String) = apiClient.updateEmail(newEmail.trim(), password)

    override suspend fun updatePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ) = apiClient.updatePassword(oldPassword.trim(), newPassword.trim(), newPasswordConfirmation.trim())

    override suspend fun allocatePoint(stat: Attribute): Stats? {
        val liveUser = getLiveUser()
        if (liveUser != null) {
            localRepository.executeTransaction {
                when (stat) {
                    Attribute.STRENGTH -> liveUser.stats?.strength = liveUser.stats?.strength?.inc()
                    Attribute.INTELLIGENCE -> liveUser.stats?.intelligence = liveUser.stats?.intelligence?.inc()
                    Attribute.CONSTITUTION -> liveUser.stats?.constitution = liveUser.stats?.constitution?.inc()
                    Attribute.PERCEPTION -> liveUser.stats?.per = liveUser.stats?.per?.inc()
                }
                liveUser.stats?.points = liveUser.stats?.points?.dec()
            }
        }
        val stats = apiClient.allocatePoint(stat.value) ?: return null
        if (liveUser != null) {
            localRepository.executeTransaction {
                liveUser.stats?.strength = stats.strength
                liveUser.stats?.constitution = stats.constitution
                liveUser.stats?.per = stats.per
                liveUser.stats?.intelligence = stats.intelligence
                liveUser.stats?.points = stats.points
                liveUser.stats?.mp = stats.mp
            }
        }
        return stats
    }

    override suspend fun bulkAllocatePoints(
        strength: Int,
        intelligence: Int,
        constitution: Int,
        perception: Int
    ): Stats? {
        val stats = apiClient.bulkAllocatePoints(
            strength,
            intelligence,
            constitution,
            perception
        ) ?: return null
        val user = getLiveUser()
        if (user != null) {
            localRepository.modify(user) { liveUser ->
                liveUser.stats?.strength = stats.strength
                liveUser.stats?.constitution = stats.constitution
                liveUser.stats?.per = stats.per
                liveUser.stats?.intelligence = stats.intelligence
                liveUser.stats?.points = stats.points
                liveUser.stats?.mp = stats.mp
            }
        }
        return stats
    }

    override suspend fun runCron(tasks: MutableList<Task>) {
        val user = getLiveUser()
        if (user != null) {
            localRepository.modify(user) { liveUser ->
                liveUser.needsCron = false
                liveUser.lastCron = Date()
            }
        }
        if (tasks.isNotEmpty()) {
            val scoringList = mutableListOf<Map<String, String>>()
            for (task in tasks) {
                val map = mutableMapOf<String, String>()
                map["id"] = task.id ?: ""
                map["direction"] = TaskDirection.UP.text
                scoringList.add(map)
            }
            taskRepository.bulkScoreTasks(scoringList)
        }
        apiClient.runCron()
        retrieveUser(true, true)
    }

    override suspend fun useCustomization(type: String, category: String?, identifier: String): User? {
        if (appConfigManager.enableLocalChanges()) {
            val liveUser = getLiveUser()
            if (liveUser != null) {
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
            }
        }
        if (type == "background") {
            apiClient.unlockPath("background.$identifier")
            return retrieveUser(false, true)
        } else {
            var updatePath = "preferences.$type"
            if (category != null) {
                updatePath = "$updatePath.$category"
            }
            return updateUser(updatePath, identifier)
        }
    }

    override suspend fun retrieveAchievements(): List<Achievement>? {
        val achievements = apiClient.getMemberAchievements(currentUserID) ?: return null
        localRepository.save(achievements)
        return achievements
    }

    override fun getAchievements(): Flow<List<Achievement>> {
        return localRepository.getAchievements()
    }

    override fun getQuestAchievements(): Flow<List<QuestAchievement>> {
        return localRepository.getQuestAchievements(currentUserID)
    }

    override suspend fun retrieveTeamPlans(): List<TeamPlan>? {
        val teams = apiClient.getTeamPlans() ?: return null
        teams.forEach { it.userID = currentUserID }
        localRepository.save(teams)
        return teams
    }

    override fun getTeamPlans(): Flow<List<TeamPlan>> {
        return localRepository.getTeamPlans(currentUserID)
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
        localRepository.save(members.map {
            GroupMembership(it.id, id)
        })
        members.let { localRepository.save(members) }
        return team
    }

    override fun getTeamPlan(teamID: String): Flow<Group?> {
        return localRepository.getTeamPlan(teamID)
            .map {
                it ?: retrieveTeamPlan(teamID)
            }
    }

    private suspend fun getLiveUser(): User? {
        val user = localRepository.getUser(currentUserID).firstOrNull() ?: return null
        return localRepository.getLiveObject(user)
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
