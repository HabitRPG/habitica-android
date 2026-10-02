package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.realm.RealmList
import java.util.Date

class TaskStateTest :
    WordSpec({
        fun assignee(
            userID: String,
            completed: Boolean = false,
        ) = GroupAssignedDetails().apply {
            assignedUserID = userID
            this.completed = completed
        }

        fun groupTask(vararg assignees: GroupAssignedDetails) =
            Task().apply {
                type = TaskType.TODO
                group =
                    TaskGroupPlan().apply {
                        groupID = "group-id"
                        assignedUsers = RealmList(*assignees.mapNotNull { it.assignedUserID }.toTypedArray())
                        assignedUsersDetail = RealmList(*assignees)
                    }
            }

        "isGroupTask" should {
            "be false without a group" {
                Task().isGroupTask shouldBe false
            }

            "be false for a blank group id" {
                Task().apply { group = TaskGroupPlan().apply { groupID = " " } }.isGroupTask shouldBe false
            }

            "be true with a group id" {
                groupTask().isGroupTask shouldBe true
            }
        }

        "completed" should {
            "use the task state for personal tasks" {
                val task = Task().apply { completed = true }
                task.completed("any-user") shouldBe true
                task.completed(null) shouldBe true
            }

            "use the state of the assigned user for group tasks" {
                val task = groupTask(assignee("user-1", true), assignee("user-2", false))
                task.completed("user-1") shouldBe true
                task.completed("user-2") shouldBe false
            }

            "fall back to the task state for users that are not assigned" {
                val task = groupTask(assignee("user-1", true))
                task.completed = false
                task.completed("other-user") shouldBe false
            }
        }

        "completeForUser" should {
            "complete personal tasks" {
                val task = Task()
                task.completeForUser("user-1", true)
                task.completed shouldBe true
                task.completeForUser("user-1", false)
                task.completed shouldBe false
            }

            "only complete the assignment of that user" {
                val task = groupTask(assignee("user-1"), assignee("user-2"))
                task.completeForUser("user-1", true)
                task.completed("user-1") shouldBe true
                task.completed("user-2") shouldBe false
                task.completed shouldBe false
            }

            "complete the whole task once every assignee is done" {
                val task = groupTask(assignee("user-1"), assignee("user-2", true))
                task.completeForUser("user-1", true)
                task.completed shouldBe true
            }

            "uncomplete the whole task once every assignee is undone" {
                val task = groupTask(assignee("user-1", true))
                task.completed = true
                task.completeForUser("user-1", false)
                task.completed shouldBe false
            }

            "complete a group task without assignees directly" {
                val task = groupTask()
                task.completeForUser("user-1", true)
                task.completed shouldBe true
            }
        }

        "isAssignedToUser" should {
            "check the assigned users" {
                val task = groupTask(assignee("user-1"))
                task.isAssignedToUser("user-1") shouldBe true
                task.isAssignedToUser("user-2") shouldBe false
                Task().isAssignedToUser("user-1") shouldBe false
            }
        }

        "isDisplayedActiveForUser" should {
            "be true for open todos" {
                Task().apply { type = TaskType.TODO }.isDisplayedActiveForUser("user") shouldBe true
            }

            "be false for completed todos" {
                Task().apply {
                    type = TaskType.TODO
                    completed = true
                }.isDisplayedActiveForUser("user") shouldBe false
            }

            "be true for open dailies that are due" {
                Task().apply {
                    type = TaskType.DAILY
                    isDue = true
                }.isDisplayedActiveForUser("user") shouldBe true
            }

            "be false for dailies that are not due" {
                Task().apply {
                    type = TaskType.DAILY
                    isDue = false
                }.isDisplayedActiveForUser("user") shouldBe false
            }

            "be false for habits and rewards" {
                Task().apply { type = TaskType.HABIT }.isDisplayedActiveForUser("user") shouldBe false
                Task().apply { type = TaskType.REWARD }.isDisplayedActiveForUser("user") shouldBe false
            }

            "respect the assignment state for group tasks" {
                val task = groupTask(assignee("user-1", true), assignee("user-2"))
                task.isDisplayedActiveForUser("user-1") shouldBe false
                task.isDisplayedActiveForUser("user-2") shouldBe true
            }
        }

        "isPendingApproval" should {
            "be true only when approval is required, requested and not yet given" {
                val task = groupTask()
                task.isPendingApproval shouldBe false
                task.group?.approvalRequired = true
                task.isPendingApproval shouldBe false
                task.group?.approvalRequested = true
                task.isPendingApproval shouldBe true
                task.group?.approvalApproved = true
                task.isPendingApproval shouldBe false
            }
        }

        "streakString" should {
            "be null without any counters or streak" {
                Task().streakString.shouldBeNull()
            }

            "show both habit counters" {
                Task().apply {
                    counterUp = 3
                    counterDown = 2
                }.streakString shouldBe "+3 | -2"
            }

            "show only the positive counter" {
                Task().apply {
                    counterUp = 3
                    counterDown = 0
                }.streakString shouldBe "+3"
            }

            "show only the negative counter" {
                Task().apply { counterDown = 4 }.streakString shouldBe "-4"
            }

            "show the streak for dailies" {
                Task().apply { streak = 12 }.streakString shouldBe "12"
            }

            "prefer counters over the streak" {
                Task().apply {
                    streak = 12
                    counterUp = 1
                }.streakString shouldBe "+1"
            }
        }

        "checklist" should {
            "count completed items" {
                val task =
                    Task().apply {
                        checklist = RealmList(ChecklistItem(completed = true), ChecklistItem(), ChecklistItem(completed = true))
                    }
                task.completedChecklistCount shouldBe 2
                task.isChecklistDisplayActive shouldBe true
            }

            "not be displayed as active when everything is completed" {
                val task = Task().apply { checklist = RealmList(ChecklistItem(completed = true)) }
                task.isChecklistDisplayActive shouldBe false
            }
        }

        "containsAllTagIds" should {
            val task =
                Task().apply {
                    tags = RealmList(Tag().apply { id = "a" }, Tag().apply { id = "b" })
                }

            "match when all tags are present" {
                task.containsAllTagIds(listOf("a")) shouldBe true
                task.containsAllTagIds(listOf("a", "b")) shouldBe true
                task.containsAllTagIds(emptyList()) shouldBe true
            }

            "not match when a tag is missing" {
                task.containsAllTagIds(listOf("a", "c")) shouldBe false
            }

            "not match when the task has no tags" {
                Task().apply { tags = null }.containsAllTagIds(listOf("a")) shouldBe false
            }
        }

        "isDayOrMorePastDue" should {
            "be null without a due date" {
                Task().isDayOrMorePastDue().shouldBeNull()
            }

            "be true for yesterday" {
                Task().apply { dueDate = Date(System.currentTimeMillis() - 86_400_000L * 2) }.isDayOrMorePastDue() shouldBe true
            }

            "be false for today and the future" {
                Task().apply { dueDate = Date() }.isDayOrMorePastDue() shouldBe false
                Task().apply { dueDate = Date(System.currentTimeMillis() + 86_400_000L * 2) }.isDayOrMorePastDue() shouldBe false
            }
        }

        "isBeingEdited" should {
            fun task(type: TaskType) =
                Task().apply {
                    this.type = type
                    text = "Text"
                    notes = "Notes"
                    priority = 1f
                }

            "be false for identical tasks" {
                TaskType.entries.forEach { type ->
                    task(type).isBeingEdited(task(type)) shouldBe false
                }
            }

            "detect common changes" {
                task(TaskType.TODO).isBeingEdited(task(TaskType.TODO).apply { text = "Other" }) shouldBe true
                task(TaskType.TODO).isBeingEdited(task(TaskType.TODO).apply { notes = "Other" }) shouldBe true
                task(TaskType.TODO).isBeingEdited(task(TaskType.TODO).apply { priority = 2f }) shouldBe true
                task(TaskType.TODO).isBeingEdited(
                    task(TaskType.TODO).apply { checklist = RealmList(ChecklistItem(text = "item")) },
                ) shouldBe true
            }

            "detect habit specific changes" {
                task(TaskType.HABIT).isBeingEdited(task(TaskType.HABIT).apply { up = true }) shouldBe true
                task(TaskType.HABIT).isBeingEdited(task(TaskType.HABIT).apply { counterUp = 5 }) shouldBe true
            }

            "detect daily specific changes" {
                task(TaskType.DAILY).isBeingEdited(task(TaskType.DAILY).apply { everyX = 3 }) shouldBe true
                task(TaskType.DAILY).isBeingEdited(task(TaskType.DAILY).apply { streak = 5 }) shouldBe true
            }

            "detect a new due date for todos" {
                task(TaskType.TODO).isBeingEdited(task(TaskType.TODO).apply { dueDate = Date() }) shouldBe true
            }

            "ignore a removed due date for todos" {
                task(TaskType.TODO).apply { dueDate = Date() }.isBeingEdited(task(TaskType.TODO)) shouldBe false
            }

            "detect reward value changes" {
                task(TaskType.REWARD).isBeingEdited(task(TaskType.REWARD).apply { value = 20.0 }) shouldBe true
            }
        }
    })
