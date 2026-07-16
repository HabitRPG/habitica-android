package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.widget.glance.work.CronBoundaryRefreshWorker
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.NumberAbbreviator
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

data class StatsWidgetState(
    val hp: Float,
    val maxHp: Float,
    val exp: Float,
    val toNextLevel: Float,
    val mp: Float,
    val maxMp: Float,
    val level: Int,
    val goldText: String,
    val gemsText: String,
    val hourglassesText: String,
    val hourglassCount: Int,
    val showMp: Boolean,
    val className: String?,
    val avatarBitmapPath: String? = null,
    val userId: String? = null,
) {
    val hpText: String get() = hp.toInt().toString()
    val maxHpText: String get() = maxHp.toInt().toString()
    val expText: String get() = exp.toInt().toString()
    val toNextLevelText: String get() = toNextLevel.toInt().toString()
    val mpText: String get() = mp.toInt().toString()
    val maxMpText: String get() = maxMp.toInt().toString()

    companion object {
        fun fromUser(context: Context, user: User, avatarBitmapPath: String? = null): StatsWidgetState? {
            val s = user.stats ?: return null
            val gold = (s.gp ?: 0.0)
            val gems = ((user.balance) * 4).toInt()
            val hourglasses = user.hourglassCount
            val classesDisabled = user.preferences?.disableClasses == true
            val showMp = !classesDisabled && s.habitClass != null && (s.lvl ?: 0) >= 10
            return StatsWidgetState(
                hp = (s.hp ?: 0.0).toFloat(),
                maxHp = (s.maxHealth ?: 50).toFloat(),
                exp = (s.exp ?: 0.0).toFloat(),
                toNextLevel = (s.toNextLevel ?: 50).toFloat(),
                mp = (s.mp ?: 0.0).toFloat(),
                maxMp = (s.maxMP ?: 50).toFloat(),
                level = s.lvl ?: 0,
                goldText = NumberAbbreviator.abbreviate(context, gold, numberOfDecimals = 0, minForAbbrevation = 1000),
                gemsText = NumberAbbreviator.abbreviate(context, gems.toDouble(), numberOfDecimals = 0, minForAbbrevation = 1000),
                hourglassesText = hourglasses.toString(),
                hourglassCount = hourglasses,
                showMp = showMp,
                className = if (classesDisabled) null else s.habitClass,
                avatarBitmapPath = avatarBitmapPath,
                userId = user.id,
            )
        }
    }
}

data class TaskListWidgetState(
    val tasks: List<TaskWidgetItem>,
    val needsCron: Boolean,
)

data class TaskWidgetItem(
    val id: String,
    val text: String,
    val value: Double,
    val checklistTotal: Int,
    val checklistDone: Int,
)

fun Task.toWidgetItem(): TaskWidgetItem {
    val list = checklist
    return TaskWidgetItem(
        id = id ?: "",
        text = text,
        value = value,
        checklistTotal = list?.size ?: 0,
        checklistDone = list?.count { it.completed } ?: 0,
    )
}

data class DailyCountWidgetState(
    val totalDue: Int,
    val completed: Int,
    val needsCron: Boolean,
)

fun computeNeedsCron(user: User?, now: Long = System.currentTimeMillis()): Boolean {
    if (user == null) return false
    if (user.needsCron) return true
    val lastCron = user.lastCron ?: return false
    val dayStart = user.preferences?.dayStart ?: 0
    return lastCron.time < CronBoundaryRefreshWorker.lastBoundaryMillis(dayStart, now)
}

suspend fun loadTaskListStateOrNull(context: Context, taskType: TaskType): TaskListWidgetState? =
    withContext(Dispatchers.Main) {
        try {
            val entry = widgetEntryPoint(context)
            entry.taskRepository().refreshLocalData()
            val user = entry.userRepository().getUser().firstOrNull() ?: return@withContext null
            val mirroredGroupIds = user.preferences?.tasks?.mirrorGroupTasks
                ?.toTypedArray() ?: emptyArray()
            val raw = entry.taskRepository().getTasks(
                taskType = taskType,
                userID = user.id,
                includedGroupIDs = mirroredGroupIds,
            ).firstOrNull() ?: return@withContext null
            val visible = raw.filter {
                !it.completed(user.id) && (taskType != TaskType.DAILY || it.isDue == true)
            }
            TaskListWidgetState(
                tasks = visible.map { it.toWidgetItem() },
                needsCron = computeNeedsCron(user),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            ExceptionHandler.reportError(throwable)
            null
        }
    }

suspend fun loadStatsStateOrNull(context: Context): StatsWidgetState? =
    withContext(Dispatchers.Main) {
        try {
            val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
                ?: return@withContext null
            AvatarBitmapCache.refreshIfNeeded(context, user)
            StatsWidgetState.fromUser(
                context = context,
                user = user,
                avatarBitmapPath = AvatarBitmapCache.cachedFile(context).absolutePath,
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            ExceptionHandler.reportError(throwable)
            null
        }
    }

suspend fun loadDailyCountStateOrNull(context: Context): DailyCountWidgetState? =
    withContext(Dispatchers.Main) {
        try {
            val entry = widgetEntryPoint(context)
            entry.taskRepository().refreshLocalData()
            val user = entry.userRepository().getUser().firstOrNull() ?: return@withContext null
            val mirroredGroupIds = user.preferences?.tasks?.mirrorGroupTasks
                ?.toTypedArray() ?: emptyArray()
            val due = (
                entry.taskRepository().getTasks(
                    taskType = TaskType.DAILY,
                    userID = user.id,
                    includedGroupIDs = mirroredGroupIds,
                ).firstOrNull() ?: return@withContext null
                ).filter { it.isDue == true }
            DailyCountWidgetState(
                totalDue = due.size,
                completed = due.count { it.completed(user.id) },
                needsCron = computeNeedsCron(user),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            ExceptionHandler.reportError(throwable)
            null
        }
    }
