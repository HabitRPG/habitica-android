package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.NumberAbbreviator

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
) {
    val hpText: String get() = hp.toInt().toString()
    val maxHpText: String get() = maxHp.toInt().toString()
    val expText: String get() = exp.toInt().toString()
    val toNextLevelText: String get() = toNextLevel.toInt().toString()
    val mpText: String get() = mp.toInt().toString()
    val maxMpText: String get() = maxMp.toInt().toString()

    companion object {
        val Empty = StatsWidgetState(
            hp = 0f, maxHp = 50f,
            exp = 0f, toNextLevel = 50f,
            mp = 0f, maxMp = 50f,
            level = 0,
            goldText = "0", gemsText = "0", hourglassesText = "0", hourglassCount = 0,
            showMp = false,
            className = null,
            avatarBitmapPath = null,
        )

        fun fromUser(context: Context, user: User?, avatarBitmapPath: String? = null): StatsWidgetState {
            val s = user?.stats ?: return Empty.copy(avatarBitmapPath = avatarBitmapPath)
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

fun computeNeedsCron(user: User?): Boolean = user?.needsCron == true
