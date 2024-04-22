package com.habitrpg.android.habitica.interactors

import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

class ScoreTaskLocallyInteractor {
    companion object {
        const val MAX_TASK_VALUE = 21.27
        const val MIN_TASK_VALUE = -47.27
        const val CLOSE_ENOUGH = 0.00001

        private fun calculateDelta(
            task: Task,
            direction: TaskDirection,
        ): Double {
            val currentValue =
                when {
                    task.value < MIN_TASK_VALUE -> MIN_TASK_VALUE
                    task.value > MAX_TASK_VALUE -> MAX_TASK_VALUE
                    else -> task.value
                }

            var nextDelta =
                0.9747.pow(currentValue) * if (direction == TaskDirection.DOWN) -1 else 1

            if ((task.checklist?.size ?: 0) > 0) {
                if (task.type == TaskType.TODO) {
                    nextDelta *= 1 + (
                        task.checklist?.map { if (it.completed) 1 else 0 }?.reduce { _, _ -> 0 }
                            ?: 0
                    )
                }
            }

            return nextDelta
        }

        private fun scoreHabit(
            user: User,
            task: Task,
            direction: TaskDirection,
        ) {
        }

        private fun scoreDaily(
            user: User,
            task: Task,
            direction: TaskDirection,
        ) {
        }

        private fun scoreToDo(
            user: User,
            task: Task,
            direction: TaskDirection,
        ) {
        }

        private fun scoreReward(
            user: User,
            task: Task,
            direction: TaskDirection,
        ) {
        }

        fun score(
            user: User,
            task: Task,
            direction: TaskDirection,
        ): TaskDirectionData? {
            return if (task.type == TaskType.HABIT || direction == TaskDirection.UP) {
                val stats = user.stats ?: return null
                val computedStats = computeStats(user)
                val result = TaskDirectionData()
                result.hp = stats.hp ?: 0.0
                result.exp = stats.exp ?: 0.0
                result.gp = stats.gp ?: 0.0
                result.mp = stats.mp ?: 0.0
                val delta = calculateDelta(task, direction)
                result.delta = delta.toFloat()
                if (delta > 0) {
                    addPoints(result, delta, stats, computedStats, task, direction)
                } else {
                    subtractPoints(result, delta, stats, computedStats, task)
                }

                when (task.type) {
                    TaskType.HABIT -> scoreHabit(user, task, direction)
                    TaskType.DAILY -> scoreDaily(user, task, direction)
                    TaskType.TODO -> scoreToDo(user, task, direction)
                    TaskType.REWARD -> scoreReward(user, task, direction)
                    else -> {}
                }

                if (result.hp <= 0.0) {
                    result.hp = 0.0
                }
                if (result.exp >= (stats.toNextLevel?.toDouble() ?: 0.0)) {
                    result.exp = result.exp - (stats.toNextLevel?.toDouble() ?: 0.0)
                    result.lvl = (user.stats?.lvl ?: 0) + 1
                    result.hp = 50.0
                } else {
                    result.lvl = user.stats?.lvl ?: 0
                }

                result
            } else {
                null
            }
        }

        private fun subtractPoints(
            result: TaskDirectionData,
            delta: Double,
            stats: Stats,
            computedStats: Stats,
            task: Task,
        ) {
            var conBonus = 1f - ((computedStats.constitution?.toFloat() ?: 0f) / 250f)
            if (conBonus < 0.1) {
                conBonus = 0.1f
            }
            val hpMod = delta * conBonus * task.priority * 2
            result.hp = (stats.hp ?: 0.0) + (hpMod * 10).roundToLong() / 10.0
        }

        private fun addPoints(
            result: TaskDirectionData,
            delta: Double,
            stats: Stats,
            computedStats: Stats,
            task: Task,
            direction: TaskDirection,
        ) {
            val intBonus = 1f + ((computedStats.intelligence?.toFloat() ?: 0f) * 0.025f)
            result.exp = (
                stats.exp
                    ?: 0.0
            ) + (delta * intBonus * task.priority * 6).roundToLong().toDouble()

            val perBonus = 1f + ((computedStats.per?.toFloat() ?: 0f) * 0.02f)
            val goldMod = delta * task.priority * perBonus

            val streak = task.streak ?: 0
            result.gp = (stats.gp ?: 0.0) +
                if (task.streak != null) {
                    val currentStreak = if (direction == TaskDirection.DOWN) streak - 1 else streak
                    val streakBonus = (currentStreak / 100) * 1
                    val afterStreak = goldMod * streakBonus
                    afterStreak
                } else {
                    goldMod
                }
        }

        private fun computeStats(user: User): Stats {
            val levelStat = min((user.stats?.lvl ?: 0) / 2.0f, 50f).toInt()

            var totalStrength = levelStat
            var totalIntelligence = levelStat
            var totalConstitution = levelStat
            var totalPerception = levelStat

            totalStrength += user.stats?.buffs?.str?.toInt() ?: 0
            totalIntelligence += user.stats?.buffs?.intelligence?.toInt() ?: 0
            totalConstitution += user.stats?.buffs?.con?.toInt() ?: 0
            totalPerception += user.stats?.buffs?.per?.toInt() ?: 0

            totalStrength += user.stats?.strength ?: 0
            totalIntelligence += user.stats?.intelligence ?: 0
            totalConstitution += user.stats?.constitution ?: 0
            totalPerception += user.stats?.per ?: 0

            val outfit = user.items?.gear?.equipped
            val outfitList = ArrayList<String>()
            outfit?.let { thisOutfit ->
                outfitList.add(thisOutfit.armor)
                outfitList.add(thisOutfit.back)
                outfitList.add(thisOutfit.body)
                outfitList.add(thisOutfit.eyeWear)
                outfitList.add(thisOutfit.head)
                outfitList.add(thisOutfit.headAccessory)
                outfitList.add(thisOutfit.shield)
                outfitList.add(thisOutfit.weapon)
            }

            val stats = Stats()
            stats.strength = totalStrength
            stats.intelligence = totalIntelligence
            stats.constitution = totalConstitution
            stats.per = totalPerception

            return stats
        }
    }
}
