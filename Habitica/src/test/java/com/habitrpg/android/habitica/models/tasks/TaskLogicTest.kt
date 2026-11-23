package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.realm.RealmList
import java.util.Date

class TaskLogicTest :
    StringSpec({
        fun assignee(
            id: String,
            completed: Boolean = false,
        ) = GroupAssignedDetails().apply {
            assignedUserID = id
            this.completed = completed
        }

        fun groupTask(vararg users: GroupAssignedDetails) =
            Task().apply {
                type = TaskType.TODO
                group =
                    TaskGroupPlan().apply {
                        groupID = "group"
                        assignedUsersDetail = RealmList(*users)
                        assignedUsers = RealmList(*users.map { it.assignedUserID }.toTypedArray())
                    }
            }

        "isGroupTask requires a non blank group id" {
            Task().isGroupTask shouldBe false
            Task().apply { group = TaskGroupPlan() }.isGroupTask shouldBe false
            Task().apply { group = TaskGroupPlan().apply { groupID = " " } }.isGroupTask shouldBe false
            groupTask().isGroupTask shouldBe true
        }

        "completed(byUser) uses the assignee's state for group tasks" {
            val task = groupTask(assignee("a", true), assignee("b", false))
            task.completed("a") shouldBe true
            task.completed("b") shouldBe false
        }

        "completed(byUser) falls back to task state for unknown users and solo tasks" {
            val task = groupTask(assignee("a", true)).apply { completed = false }
            task.completed("stranger") shouldBe false
            task.completed(null) shouldBe false
            Task().apply { completed = true }.completed("anyone") shouldBe true
        }

        "completeForUser completes the task only once every assignee is done" {
            val task = groupTask(assignee("a"), assignee("b"))
            task.completeForUser("a", true)
            task.completed("a") shouldBe true
            task.completed shouldBe false
            task.completeForUser("b", true)
            task.completed shouldBe true
        }

        "completeForUser on a group task without assignees sets the task state" {
            val task = groupTask()
            task.completeForUser("a", true)
            task.completed shouldBe true
        }

        "completeForUser ignores users that are not assigned" {
            val task = groupTask(assignee("a"), assignee("b"))
            task.completeForUser("stranger", true)
            task.completed shouldBe false
            task.completed("a") shouldBe false
        }

        "isDisplayedActiveForUser" {
            val task = groupTask(assignee("a", true), assignee("b", false))
            task.isDisplayedActiveForUser("a") shouldBe false
            task.isDisplayedActiveForUser("b") shouldBe true
            Task().apply {
                type = TaskType.DAILY
                isDue = false
            }.isDisplayedActiveForUser(null) shouldBe false
            Task().apply { type = TaskType.HABIT }.isDisplayedActiveForUser(null) shouldBe false
        }

        "isAssignedToUser" {
            groupTask(assignee("a")).isAssignedToUser("a") shouldBe true
            groupTask(assignee("a")).isAssignedToUser("b") shouldBe false
            Task().isAssignedToUser("a") shouldBe false
        }

        "isPendingApproval needs required, requested and not yet approved" {
            fun pending(
                required: Boolean?,
                requested: Boolean?,
                approved: Boolean?,
            ) = Task()
                .apply {
                    group =
                        TaskGroupPlan().apply {
                            approvalRequired = required
                            approvalRequested = requested
                            approvalApproved = approved
                        }
                }.isPendingApproval

            pending(true, true, false) shouldBe true
            pending(true, true, true) shouldBe false
            pending(true, false, false) shouldBe false
            pending(false, true, false) shouldBe false
            // unknown approval state is not treated as pending
            pending(true, true, null) shouldBe false
            Task().isPendingApproval shouldBe false
        }

        "checklist counts" {
            val task =
                Task().apply {
                    checklist =
                        RealmList(
                            ChecklistItem().apply { completed = true },
                            ChecklistItem(),
                        )
                }
            task.completedChecklistCount shouldBe 1
            task.isChecklistDisplayActive shouldBe true
            task.checklist?.get(1)?.completed = true
            task.isChecklistDisplayActive shouldBe false
        }

        "empty or missing checklist is not displayed active" {
            Task().isChecklistDisplayActive shouldBe false
            Task().apply { checklist = null }.completedChecklistCount shouldBe 0
        }

        "containsAllTagIds" {
            val task = Task().apply { tags = RealmList(Tag().apply { id = "1" }, Tag().apply { id = "2" }) }
            task.containsAllTagIds(listOf("1")) shouldBe true
            task.containsAllTagIds(listOf("1", "2")) shouldBe true
            task.containsAllTagIds(listOf("1", "3")) shouldBe false
            task.containsAllTagIds(emptyList()) shouldBe true
            Task().apply { tags = null }.containsAllTagIds(emptyList()) shouldBe false
        }

        "streakString" {
            Task().apply {
                counterUp = 2
                counterDown = 1
            }.streakString shouldBe "+2 | -1"
            Task().apply { counterDown = 4 }.streakString shouldBe "-4"
            Task().apply { streak = 3 }.streakString shouldBe "3"
            Task().apply {
                counterUp = 0
                counterDown = 0
                streak = 0
            }.streakString shouldBe null
        }

        "isUpdatedToday" {
            Task().isUpdatedToday shouldBe false
            Task().apply { updatedAt = Date() }.isUpdatedToday shouldBe true
            Task().apply { updatedAt = Date(0) }.isUpdatedToday shouldBe false
        }

        "isDayOrMorePastDue" {
            Task().isDayOrMorePastDue() shouldBe null
            Task().apply { dueDate = Date() }.isDayOrMorePastDue() shouldBe false
            Task().apply { dueDate = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L) }.isDayOrMorePastDue() shouldBe true
        }

        "days and weeks of month are mutually exclusive" {
            val task = Task()
            task.setDaysOfMonth(listOf(1, 15))
            task.getDaysOfMonth() shouldBe listOf(1, 15)
            task.daysOfMonthString shouldBe "[1, 15]"

            task.setWeeksOfMonth(listOf(2))
            task.getWeeksOfMonth() shouldBe listOf(2)
            task.weeksOfMonthString shouldBe "[2]"
            task.daysOfMonthString shouldBe "[]"
        }

        "setting empty days of month keeps weeks of month" {
            val task = Task()
            task.setWeeksOfMonth(listOf(3))
            task.setDaysOfMonth(emptyList())
            task.daysOfMonthString shouldBe "[]"
            task.getWeeksOfMonth() shouldBe listOf(3)
        }

        "equals compares ids and hashCode follows" {
            val a = Task().apply { id = "1" }
            val b = Task().apply { id = "1" }
            val c = Task().apply { id = "2" }
            (a == b) shouldBe true
            (a == c) shouldBe false
            a.equals(null) shouldBe false
            a.equals("1") shouldBe false
            a.hashCode() shouldBe b.hashCode()
            Task().apply { id = null }.hashCode() shouldBe 0
        }

        "isBeingEdited detects changed text, notes and checklist" {
            fun base() =
                Task().apply {
                    type = TaskType.TODO
                    text = "a"
                    notes = "n"
                }
            base().isBeingEdited(base()) shouldBe false
            base().isBeingEdited(base().apply { text = "b" }) shouldBe true
            base().isBeingEdited(base().apply { notes = "m" }) shouldBe true
            base().isBeingEdited(base().apply { checklist = RealmList(ChecklistItem()) }) shouldBe true
            base().isBeingEdited(base().apply { priority = 2f }) shouldBe true
        }

        "isBeingEdited detects type specific changes" {
            fun habit() = Task().apply { type = TaskType.HABIT }
            habit().isBeingEdited(habit().apply { up = true }) shouldBe true
            habit().isBeingEdited(habit()) shouldBe false

            fun daily() = Task().apply { type = TaskType.DAILY }
            daily().isBeingEdited(daily().apply { everyX = 3 }) shouldBe true

            fun reward() = Task().apply { type = TaskType.REWARD }
            reward().isBeingEdited(reward().apply { value = 30.0 }) shouldBe true

            fun todo() = Task().apply { type = TaskType.TODO }
            todo().isBeingEdited(todo().apply { dueDate = Date(0) }) shouldBe true
        }
    })
