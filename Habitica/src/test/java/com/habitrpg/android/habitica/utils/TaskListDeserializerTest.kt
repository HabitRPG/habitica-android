package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.tasks.TaskList
import io.kotest.matchers.shouldBe

class TaskListDeserializerTest :
    SerializerSpec({

        "deserialize" should {
            "leave a thin, id-only placeholder for tag entries that are raw id strings" {
                val json = """[{"_id": "task-1", "tags": ["tag-1", "tag-2"]}]"""
                val result = parse<TaskList>(json)
                val task = result.tasks["task-1"]
                task?.tags?.map { it.id } shouldBe listOf("tag-1", "tag-2")
                task?.tags?.all { it.name.isEmpty() } shouldBe true
            }

            "use the full tag object when the entry is already a JSON object" {
                val json = """[{"_id": "task-1", "tags": [{"id": "tag-1", "name": "Work", "userId": "user-1"}]}]"""
                val result = parse<TaskList>(json)
                val tag = result.tasks["task-1"]?.tags?.firstOrNull()
                tag?.id shouldBe "tag-1"
                tag?.name shouldBe "Work"
            }

            "not require any database access" {
                val json = """[{"_id": "task-1", "tags": ["tag-1"]}]"""
                parse<TaskList>(json)
            }
        }
    })
