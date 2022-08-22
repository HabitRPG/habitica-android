package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.github.kakaocup.kakao.common.assertions.BaseAssertions
import io.github.kakaocup.kakao.common.matchers.ChildCountMatcher
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.picker.date.KDatePickerDialog
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.spinner.KSpinner
import io.github.kakaocup.kakao.spinner.KSpinnerItem
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.toolbar.KToolbar
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import java.util.Date
import java.util.UUID
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class TaskFormScreen : Screen<TaskFormScreen>() {
    val toolbar = KToolbar { withId(R.id.toolbar) }
    val textEditText = KEditText { withId(R.id.text_edit_text) }
    val notesEditText = KEditText { withId(R.id.notes_edit_text) }
    val taskDifficultyButtons = KView { withId(R.id.task_difficulty_buttons) }
    val tagsWrapper = KView { withId(R.id.tags_wrapper) }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class TaskFormActivityTest : ActivityTestCase() {

    val screen = TaskFormScreen()

    lateinit var scenario: ActivityScenario<TaskFormActivity>

    val taskSlot = slot<Task>()

    @After
    fun cleanup() {
        scenario.close()
    }

    @Before
    fun setup() {
        every { sharedPreferences.getString("FirstDayOfTheWeek", any()) } returns "-1"
        val mockComponent: UserComponent = mockk(relaxed = true)
        every { mockComponent.inject(any<TaskFormActivity>()) } answers { initializeInjects(this.args.first()) }
        mockkObject(HabiticaBaseApplication)
        every { HabiticaBaseApplication.userComponent } returns mockComponent
    }

    private fun hasBasicTaskEditingViews() {
        screen {
            textEditText.isVisible()
            notesEditText.isVisible()
            taskDifficultyButtons.isVisible()
            tagsWrapper.isVisible()
        }
    }

    @Test
    fun showsHabitForm() {
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.HABIT.value)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            device.activities.isCurrent(TaskFormActivity::class.java)
            toolbar.hasTitle("Create Habit")
            hasBasicTaskEditingViews()
            KView { withId(R.id.habit_scoring_buttons) }.isVisible()
            KView { withId(R.id.habit_reset_streak_buttons) }.isVisible()
        }
    }

    @Test
    fun showsDailyForm() {
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.DAILY.value)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            device.activities.isCurrent(TaskFormActivity::class.java)
            toolbar.hasTitle("Create Daily")
            hasBasicTaskEditingViews()
            KView { withId(R.id.checklist_container) }.isVisible()
            KView { withId(R.id.task_scheduling_controls) }.isVisible()
            KView { withId(R.id.reminders_container) }.isVisible()
        }
    }

    @Test
    fun showsToDoForm() {
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.TODO.value)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            device.activities.isCurrent(TaskFormActivity::class.java)
            toolbar.hasTitle("Create To Do")
            hasBasicTaskEditingViews()
            KView { withId(R.id.checklist_container) }.isVisible()
            KView { withId(R.id.task_scheduling_controls) }.isVisible()
            KView { withId(R.id.reminders_container) }.isVisible()
        }
    }

    @Test
    fun showsRewardForm() {
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.REWARD.value)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            device.activities.isCurrent(TaskFormActivity::class.java)
            toolbar.hasTitle("Create Reward")
            textEditText.isVisible()
            notesEditText.isVisible()
            tagsWrapper.isVisible()
            KView { withId(R.id.reward_value) }.isVisible()
        }
    }

    @Test
    fun savesNewTask() {
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.HABIT.value)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            device.activities.isCurrent(TaskFormActivity::class.java)
            textEditText.typeText("New Habit")
            KButton { withId(R.id.action_save) }.click()
            verify(exactly = 1) { taskRepository.createTaskInBackground(any()) }
        }
    }

    @Test
    fun savesExistingTask() {
        val task = Task()
        task.id = UUID.randomUUID().toString()
        task.text = "Task text"
        task.type = TaskType.HABIT
        task.priority = 1.0f
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.HABIT.value)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, task.id!!)
        every { taskRepository.getUnmanagedTask(any()) } returns Flowable.just(task)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            toolbar {
                KView { withId(R.id.action_save) }.click()
                verify(exactly = 1) { taskRepository.updateTaskInBackground(any()) }
            }
        }
    }

    @Test
    fun deletesExistingTask() {
        val task = Task()
        task.id = UUID.randomUUID().toString()
        task.text = "Task text"
        task.type = TaskType.DAILY
        task.priority = 1.0f
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.DAILY.value)
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.DAILY.value)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, task.id!!)
        every { taskRepository.getUnmanagedTask(any()) } returns Flowable.just(task)

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            device.activities.isCurrent(TaskFormActivity::class.java)
            KButton { withId(R.id.action_delete) }.click()
            KButton { withText(R.string.delete_task) }.click()
            verify(exactly = 1) { taskRepository.deleteTask(task.id!!) }
        }
    }

    /* TODO: Revisit this. For some reason the matchers can't find the checklist add button

    @Test
    fun testChecklistItems() {
        before {
            val bundle = Bundle()
            bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.DAILY.value)

            val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
            intent.putExtras(bundle)
            scenario = launchActivity(intent)
            justRun { taskRepository.createTaskInBackground(capture(taskSlot)) }
        }.after {  }.run {
            screen {

                KView { withId(R.id.checklist_container) } perform {
                    val container = this
                    step("Add new Checklist Item") {
                        hasChildCount(1)
                        KView {
                            withIndex(0) { withParent { this.getViewMatcher() } }
                        } perform {
                            click()
                            KEditText { withId(R.id.edit_text) }.typeText("test")
                            container.hasChildCount(2)
                        }
                        KEditText {
                            withIndex(1) { withId(R.id.edit_text) }
                        } perform {
                            click()
                            typeText("test2")
                            container.hasChildCount(3)
                        }
                    }
                    step("Edit Checklist Item") {
                        container.hasChildCount(3)
                        KEditText {
                            withIndex(0) { withId(R.id.edit_text) }
                        } perform {
                            clearText()
                            typeText("Test Text")
                            hasText("Test Text")
                        }
                    }
                    step("Remove Checklist Item") {
                        container.hasChildCount(3)
                        KView { withContentDescription(R.string.delete_checklist_entry) }.click()
                        container.hasChildCount(2)
                    }
                    step("Save Checklist") {
                        KButton { withId(R.id.action_save) }.click()
                        verify { taskRepository.createTaskInBackground(any()) }
                        assert(taskSlot.captured.checklist!!.size == 1)
                        assert(taskSlot.captured.checklist!!.first()!!.text == "Test Text")
                    }
                }

            }
        }
    }*/

    @Test
    fun changesScheduling() {
        val task = Task()
        task.id = UUID.randomUUID().toString()
        task.text = "Task text"
        task.type = TaskType.DAILY
        task.priority = 1.0f
        task.everyX = 1
        task.frequency = Frequency.DAILY
        task.startDate = Date()
        val bundle = Bundle()
        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, TaskType.DAILY.value)
        bundle.putString(TaskFormActivity.TASK_ID_KEY, task.id!!)
        every { taskRepository.getUnmanagedTask(any()) } returns Flowable.just(task)
        justRun { taskRepository.updateTaskInBackground(capture(taskSlot)) }

        val intent = Intent(ApplicationProvider.getApplicationContext(), TaskFormActivity::class.java)
        intent.putExtras(bundle)
        scenario = launchActivity(intent)
        screen {
            KView { withId(R.id.start_date_wrapper) }.click()
            KDatePickerDialog() perform {
                datePicker.setDate(2021, 10, 2)
                okButton.click()
            }
            KSpinner(
                builder = { withId(R.id.repeats_every_spinner) },
                itemTypeBuilder = { itemType(::KSpinnerItem) }
            ) perform {
                open()
                childAt<KSpinnerItem>(1) {
                    click()
                }
            }
            KEditText { withId(R.id.repeats_every_edittext) } perform {
                clearText()
                typeText("3")
            }
            KButton { withId(R.id.action_save) }.click()
            verify { taskRepository.updateTaskInBackground(any()) }
            assert(taskSlot.captured.everyX == 3)
            assert(taskSlot.captured.frequency == Frequency.WEEKLY)
        }
    }
}

private fun BaseAssertions.hasChildCount(count: Int) {
    matches { ViewAssertions.matches(ChildCountMatcher(count)) }
}
