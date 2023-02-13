package com.habitrpg.wearos.habitica.data.repositories

import app.cash.turbine.test
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class TaskLocalRepositoryTest : WordSpec({
 val repository = TaskLocalRepository()
 val list = TaskList()
 list.tasks["1"] = Task().apply {
  id = "1"
  type = TaskType.HABIT
 }
 list.tasks["2"] = Task().apply {
  id = "2"
  type = TaskType.DAILY
 }
 list.tasks["3"] = Task().apply {
  id = "3"
  type = TaskType.DAILY
 }
 list.tasks["4"] = Task().apply {
  id = "4"
  type = TaskType.REWARD
 }
 "getTask" should {
  "return right task" {
   repository.getTask("3").test {
    awaitItem()?.id shouldBe "3"
   }
  }
 }
})
