package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.realm.RealmList
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Date

class TaskSerializerTest :
    SerializerSpec({
        "deserialize" should {
            "read a daily with schedule, checklist and reminders" {
                val json = """{
                    "_id": "task-1", "text": "Daily", "notes": "Notes", "userId": "user-1",
                    "value": 2.5, "type": "daily", "frequency": "monthly", "attribute": "int",
                    "everyX": 2, "priority": 1.5, "completed": true, "streak": 4,
                    "challenge": {"id": "challenge-1", "broken": "TASK_DELETED"},
                    "startDate": "2015-09-28T13:00:00.000Z", "isDue": true,
                    "nextDue": ["2015-09-28T13:00:00.000Z", "2015-09-29T13:00:00.000Z"],
                    "checklist": [{"id": "c1", "text": "Item", "completed": true}],
                    "reminders": [{"id": "r1", "startDate": "s", "time": "t"}],
                    "repeat": {"m": false, "t": true},
                    "daysOfMonth": [1, 15], "weeksOfMonth": []
                }"""
                val task = parse<Task>(json)
                task.id shouldBe "task-1"
                task.text shouldBe "Daily"
                task.notes shouldBe "Notes"
                task.ownerID shouldBe "user-1"
                task.value shouldBe 2.5
                task.type shouldBe TaskType.DAILY
                task.frequency shouldBe Frequency.MONTHLY
                task.attribute shouldBe Attribute.INTELLIGENCE
                task.everyX shouldBe 2
                task.priority shouldBe 1.5f
                task.completed shouldBe true
                task.streak shouldBe 4
                task.challengeID shouldBe "challenge-1"
                task.challengeBroken shouldBe "TASK_DELETED"
                task.startDate shouldBe Date(1443445200000)
                task.isDue shouldBe true
                task.nextDue?.size shouldBe 2
                task.checklist?.map { Triple(it.id, it.text, it.completed) } shouldBe listOf(Triple("c1", "Item", true))
                task.reminders?.map { Triple(it.id, it.startDate, it.time) } shouldBe listOf(Triple("r1", "s", "t"))
                task.repeat?.m shouldBe false
                task.repeat?.t shouldBe true
                task.getDaysOfMonth() shouldBe listOf(1, 15)
            }

            "read habit counters and default missing values" {
                val json = """{"_id": "task-1", "type": "habit", "up": true, "counterUp": 3, "counterDown": 1, "streak": null}"""
                val task = parse<Task>(json)
                task.up shouldBe true
                task.down shouldBe false
                task.counterUp shouldBe 3
                task.counterDown shouldBe 1
                task.streak.shouldBeNull()
                task.priority shouldBe 1.0f
                task.value shouldBe 0.0
            }

            "fall back to habit for unknown types" {
                parse<Task>("""{"type": "unknown"}""").type shouldBe TaskType.HABIT
            }

            "read group approval flags only for tasks with a group id" {
                val json = """{"type": "todo", "group": {"id": "group-1", "approval": {"requested": true, "approved": "yes", "required": true}}}"""
                val group = parse<Task>(json).group
                group?.groupID shouldBe "group-1"
                group?.approvalRequested shouldBe true
                group?.approvalApproved shouldBe false
                group?.approvalRequired shouldBe true
            }

            "ignore a challenge without an id" {
                parse<Task>("""{"challenge": {"broken": "x"}}""").challengeID.shouldBeNull()
            }

            "return an empty task for non-object input" {
                parse(TaskSerializer, "null").id.shouldBeNull()
            }
        }

        "serialize" should {
            fun baseTask(taskType: TaskType) =
                Task().apply {
                    id = "task-1"
                    text = "Text"
                    type = taskType
                    attribute = Attribute.STRENGTH
                    tags = RealmList(Tag().apply { id = "tag-1" })
                    checklist = RealmList(ChecklistItem("c1", "Item", true))
                    reminders =
                        RealmList(
                            RemindersItem().apply {
                                id = "r1"
                                time = "t"
                            },
                        )
                }

            "write habit specific fields" {
                val task =
                    baseTask(TaskType.HABIT).apply {
                        up = true
                        down = false
                        counterUp = 2
                    }
                val obj = serialize<Task>(task)
                obj["_id"]!!.jsonPrimitive.content shouldBe "task-1"
                obj["type"]!!.jsonPrimitive.content shouldBe "habit"
                obj["attribute"]!!.jsonPrimitive.content shouldBe "str"
                obj["tags"]!!.jsonArray.map { it.jsonPrimitive.content } shouldBe listOf("tag-1")
                obj["up"]!!.jsonPrimitive.boolean shouldBe true
                obj["counterUp"]!!.jsonPrimitive.int shouldBe 2
                obj.containsKey("checklist") shouldBe false
            }

            "write an empty date and the checklist for todos" {
                val obj = serialize<Task>(baseTask(TaskType.TODO))
                obj["date"]!!.jsonPrimitive.content shouldBe ""
                obj["checklist"]!!
                    .jsonArray
                    .single()
                    .jsonObject["id"]!!
                    .jsonPrimitive.content shouldBe "c1"
                obj["reminders"]!!
                    .jsonArray
                    .single()
                    .jsonObject
                    .containsKey("startDate") shouldBe false
                obj["completed"]!!.jsonPrimitive.boolean shouldBe false
            }

            "write the due date for todos" {
                val task = baseTask(TaskType.TODO).apply { dueDate = Date(1443445200000) }
                val obj = serialize<Task>(task)
                obj["date"]!!.jsonPrimitive.content shouldBe "2015-09-28T13:00:00.000Z"
            }

            "write the schedule for dailies" {
                val task =
                    baseTask(TaskType.DAILY).apply {
                        everyX = 3
                        frequency = Frequency.WEEKLY
                        streak = 5
                    }
                val obj = serialize<Task>(task)
                obj["everyX"]!!.jsonPrimitive.int shouldBe 3
                obj["frequency"]!!.jsonPrimitive.content shouldBe "weekly"
                obj["streak"]!!.jsonPrimitive.int shouldBe 5
                obj.containsKey("checklist") shouldBe true
                obj.containsKey("up") shouldBe false
            }

            "write only common fields for rewards" {
                val obj = serialize<Task>(baseTask(TaskType.REWARD))
                obj.containsKey("checklist") shouldBe false
                obj.containsKey("up") shouldBe false
            }
        }
    })
