package com.habitrpg.android.habitica.interactors

import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Buffs
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.realm.RealmList
import kotlin.math.pow

// Expected values follow the server's scoreTask implementation that this interactor mirrors.
class ScoreTaskLocallyInteractorTest :
    WordSpec({
        lateinit var user: User
        lateinit var task: Task

        beforeEach {
            user = User()
            user.stats =
                Stats().apply {
                    lvl = 1
                    hp = 50.0
                    exp = 0.0
                    gp = 0.0
                    mp = 0.0
                    toNextLevel = 50
                }
            task =
                Task().apply {
                    type = TaskType.HABIT
                    priority = 1f
                    value = 0.0
                    streak = 0
                }
        }

        fun score(direction: TaskDirection = TaskDirection.UP) = ScoreTaskLocallyInteractor.score(user, task, direction).shouldNotBeNull()

        "score" should {
            "return null when the user has no stats" {
                user.stats = null
                ScoreTaskLocallyInteractor.score(user, task, TaskDirection.UP).shouldBeNull()
            }

            "return null when unchecking a non-habit task" {
                task.type = TaskType.DAILY
                ScoreTaskLocallyInteractor.score(user, task, TaskDirection.DOWN).shouldBeNull()
                task.type = TaskType.TODO
                ScoreTaskLocallyInteractor.score(user, task, TaskDirection.DOWN).shouldBeNull()
            }

            "award base exp and gold for a neutral task" {
                val result = score()
                result.delta shouldBe 1f
                result.exp shouldBe 6.0
                result.gp shouldBe (1.0 plusOrMinus 0.0001)
                result.hp shouldBe 50.0
                result.lvl shouldBe 1
            }

            "scale rewards with task priority" {
                task.priority = 2f
                val result = score()
                result.exp shouldBe 12.0
                result.gp shouldBe (2.0 plusOrMinus 0.0001)
            }

            "add rewards on top of existing stats" {
                user.stats?.exp = 10.0
                user.stats?.gp = 100.0
                val result = score()
                result.exp shouldBe 16.0
                result.gp shouldBe (101.0 plusOrMinus 0.0001)
            }

            "give smaller rewards for tasks with a high value" {
                task.value = 10.0
                val result = score()
                result.delta.toDouble() shouldBe (0.9747.pow(10.0) plusOrMinus 0.0001)
                result.exp shouldBe 5.0
            }

            "clamp the task value to the maximum" {
                task.value = 100.0
                score().delta.toDouble() shouldBe
                    (0.9747.pow(ScoreTaskLocallyInteractor.MAX_TASK_VALUE) plusOrMinus 0.0001)
            }

            "clamp the task value to the minimum" {
                task.value = -100.0
                score().delta.toDouble() shouldBe
                    (0.9747.pow(ScoreTaskLocallyInteractor.MIN_TASK_VALUE) plusOrMinus 0.0001)
            }

            "boost exp with intelligence" {
                user.stats?.intelligence = 40
                score().exp shouldBe 12.0
            }

            "boost gold with perception" {
                user.stats?.per = 50
                score().gp shouldBe (2.0 plusOrMinus 0.0001)
            }

            "include level based stat bonus" {
                // Every two levels give one point in each stat
                user.stats?.lvl = 80
                user.stats?.toNextLevel = 1000
                // intBonus = 1 + 40 * 0.025 = 2
                score().exp shouldBe 12.0
            }

            "cap level based stat bonus at 50" {
                user.stats?.lvl = 400
                user.stats?.toNextLevel = 1000
                // intBonus = 1 + 50 * 0.025 = 2.25
                score().exp shouldBe 14.0
            }

            "include buffs in stat bonus" {
                user.stats?.buffs = Buffs().apply { intelligence = 40f }
                score().exp shouldBe 12.0
            }

            "multiply rewards by completed checklist items for todos" {
                task.type = TaskType.TODO
                task.checklist =
                    RealmList(
                        ChecklistItem(completed = true),
                        ChecklistItem(completed = true),
                        ChecklistItem(completed = false),
                    )
                val result = score()
                result.delta shouldBe 3f
                result.exp shouldBe 18.0
            }

            "not apply checklist bonus to dailies" {
                task.type = TaskType.DAILY
                task.checklist = RealmList(ChecklistItem(completed = true), ChecklistItem(completed = true))
                score().delta shouldBe 1f
            }

            "not apply checklist bonus when nothing is completed" {
                task.type = TaskType.TODO
                task.checklist = RealmList(ChecklistItem(completed = false), ChecklistItem(completed = false))
                score().delta shouldBe 1f
            }

            "award gold for dailies without a streak" {
                task.type = TaskType.DAILY
                task.streak = 0
                score().gp shouldBe (1.0 plusOrMinus 0.0001)
            }

            "award a streak bonus for dailies" {
                task.type = TaskType.DAILY
                task.streak = 50
                score().gp shouldBe (1.5 plusOrMinus 0.0001)
            }

            "award gold when streak is unset" {
                task.streak = null
                score().gp shouldBe (1.0 plusOrMinus 0.0001)
            }

            "level up when exp reaches the next level" {
                user.stats?.exp = 45.0
                user.stats?.hp = 10.0
                val result = score()
                result.lvl shouldBe 2
                result.exp shouldBe 1.0
                result.hp shouldBe 50.0
            }

            "not level up just below the next level" {
                user.stats?.exp = 43.0
                val result = score()
                result.lvl shouldBe 1
                result.exp shouldBe 49.0
            }
        }

        "score with negative habit" should {
            "remove health" {
                val result = score(TaskDirection.DOWN)
                result.delta shouldBe -1f
                result.hp shouldBe 48.0
                result.exp shouldBe 0.0
                result.gp shouldBe 0.0
            }

            "scale damage with priority" {
                task.priority = 1.5f
                score(TaskDirection.DOWN).hp shouldBe 47.0
            }

            "reduce damage with constitution" {
                user.stats?.constitution = 125
                // conBonus = 1 - 125 / 250 = 0.5
                score(TaskDirection.DOWN).hp shouldBe 49.0
            }

            "reduce damage by at most 90%" {
                user.stats?.constitution = 1000
                score(TaskDirection.DOWN).hp shouldBe (49.8 plusOrMinus 0.0001)
            }

            "not reduce health below zero" {
                user.stats?.hp = 1.0
                score(TaskDirection.DOWN).hp shouldBe 0.0
            }
        }
    })
