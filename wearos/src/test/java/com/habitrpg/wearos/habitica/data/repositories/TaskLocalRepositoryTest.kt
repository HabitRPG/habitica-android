package com.habitrpg.wearos.habitica.data.repositories

import app.cash.turbine.test
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class TaskLocalRepositoryTest : WordSpec({
    val repository = TaskLocalRepository()
    beforeEach {
        val list = TaskList()
        list.tasks["1"] = Task().apply {
            id = "1"
            type = TaskType.HABIT
        }
        list.tasks["2"] = Task().apply {
            id = "2"
            type = TaskType.DAILY
            isDue = true
        }
        list.tasks["3"] = Task().apply {
            id = "3"
            type = TaskType.DAILY
            completed = true
            isDue = true
        }
        list.tasks["4"] = Task().apply {
            id = "4"
            type = TaskType.DAILY
            completed = false
            isDue = true
        }
        list.tasks["5"] = Task().apply {
            id = "5"
            type = TaskType.TODO
        }
        list.tasks["6"] = Task().apply {
            id = "6"
            type = TaskType.TODO
            completed = true
        }
        list.tasks["7"] = Task().apply {
            id = "7"
            type = TaskType.REWARD
        }
        repository.saveTasks(list, null)
    }
    "getTask" should {
        "return right task" {
            repository.getTask("3").test {
                awaitItem()?.id shouldBe "3"
                awaitComplete()
            }
        }
    }

    "updateTask" should {
        "update an existing task" {
            val task = Task().apply {
                id = "3"
                type = TaskType.DAILY
                completed = false
            }
            repository.updateTask(task)
            repository.getTask("3").test {
                awaitItem()?.completed shouldBe false
                awaitComplete()
            }
        }

        "add new item if task does not exist" {
            val task = Task().apply {
                id = "33"
                type = TaskType.DAILY
            }
            repository.updateTask(task)
            repository.getTaskCounts().test {
                awaitItem()[TaskType.DAILY.value] shouldBe 4
            }
        }
    }

    "getTaskCounts" should {
        "return task counts for all types" {
            repository.getTaskCounts().test {
                val counts = awaitItem()
                counts[TaskType.HABIT.value] shouldBe 1
                counts[TaskType.DAILY.value] shouldBe 3
                counts[TaskType.TODO.value] shouldBe 2
                counts[TaskType.REWARD.value] shouldBe 1
            }
        }
    }

    "getActiveTaskCounts" should {
        "return only return active task counts for all types" {
            repository.getActiveTaskCounts().test {
                val counts = awaitItem()
                counts[TaskType.HABIT.value] shouldBe 1
                counts[TaskType.DAILY.value] shouldBe 2
                counts[TaskType.TODO.value] shouldBe 1
                counts[TaskType.REWARD.value] shouldBe 1
            }
        }
    }
})
