package com.habitrpg.android.habitica.ui.fragments.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.android.habitica.ui.fragments.FragmentTestCase
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import org.hamcrest.Matcher
import org.junit.Test

open class TaskItem(val parent: Matcher<View>) : KRecyclerItem<TaskItem>(parent) {
    val title = KTextView(parent) { withId(R.id.checkedTextView) }
    val notes = KTextView(parent) { withId(R.id.notesTextView) }
}

class TaskListScreen : Screen<TaskListScreen>() {
    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.recyclerView)
    }, itemTypeBuilder = {
            itemType(::TaskItem)
        })
}

internal class TaskRecyclerViewFragmentTest : FragmentTestCase<TaskRecyclerViewFragment, FragmentRefreshRecyclerviewBinding, TaskListScreen>(false) {

    lateinit var tasks: MutableCollection<Task>

    override fun makeFragment() {
        tasks = loadJsonFile<TaskList>("tasks", TaskList::class.java).tasks.values
        fragment = spyk()
        fragment.shouldInitializeComponent = false
    }

    override fun launchFragment(args: Bundle?) {
        scenario = launchFragmentInContainer(args, R.style.MainAppTheme) {
            return@launchFragmentInContainer fragment
        }
    }

    override val screen = TaskListScreen()

    @Test
    fun displaysHabits() {
        every { taskRepository.getTasks(TaskType.HABIT, any()) } returns Flowable.just(tasks.filter { it.type == TaskType.HABIT })
        fragment.taskType = TaskType.HABIT
        launchFragment()
        screen {
            recycler {
                isVisible()
                firstChild<TaskItem> {
                    title.containsText("Check in with team")
                }
                scrollToEnd()
                lastChild<TaskItem> {
                    title.containsText("Exercise with a buddy")
                }
            }
        }
    }

    @Test
    fun displaysDailies() {
        every { taskRepository.getTasks(TaskType.DAILY, any()) } returns Flowable.just(tasks.filter { it.type == TaskType.DAILY })
        fragment.taskType = TaskType.DAILY
        launchFragment()
        screen {
            recycler {
                isVisible()
                firstChild<TaskItem> {
                    title.containsText("1")
                }
            }
        }
    }

    @Test
    fun displaysTodos() {
        every { taskRepository.getTasks(TaskType.TODO, any()) } returns Flowable.just(tasks.filter { it.type == TaskType.TODO })
        fragment.taskType = TaskType.TODO
        launchFragment()
        screen {
            recycler {
                isVisible()
                firstChild<TaskItem> {
                    title.containsText("test. todo")
                }
            }
        }
    }

    @Test
    fun displaysEmptyHabitScreen() {
        fragment.taskType = TaskType.HABIT
        launchFragment()
        screen {
            recycler {
                isVisible()
                KView { withText(R.string.empty_title_habits) }.isVisible()
            }
        }
    }

    @Test
    fun displaysEmptyDailyScreen() {
        fragment.taskType = TaskType.DAILY
        launchFragment()
        screen {
            recycler {
                isVisible()
                KView { withText(R.string.empty_title_dailies) }.isVisible()
            }
        }
    }

    @Test
    fun displaysEmptyTodoScreen() {
        fragment.taskType = TaskType.TODO
        launchFragment()
        screen {
            recycler {
                isVisible()
                KView { withText(R.string.empty_title_todos) }.isVisible()
            }
        }
    }

    @Test
    fun displaysEmptyFilteredHabitScreen() {
        fragment.taskType = TaskType.HABIT
        fragment.viewModel?.setActiveFilter(TaskType.HABIT, Task.FILTER_WEAK)
        launchFragment()
        screen {
            recycler {
                isVisible()
                KView { withText(R.string.empty_title_habits_filtered) }.isVisible()
            }
        }
    }

    @Test
    fun displaysEmptyFilteredDailyScreen() {
        fragment.taskType = TaskType.DAILY
        fragment.viewModel?.setActiveFilter(TaskType.DAILY, Task.FILTER_GRAY)
        launchFragment()
        screen {
            recycler {
                isVisible()
                KView { withText(R.string.empty_title_dailies_filtered) }.isVisible()
            }
        }
    }

    @Test
    fun displaysEmptyFilteredTodoScreen() {
        fragment.taskType = TaskType.TODO
        fragment.viewModel?.tags = mutableListOf("test")
        launchFragment()
        screen {
            recycler {
                isVisible()
                KView { withText(R.string.empty_title_todos_filtered) }.isVisible()
            }
        }
    }

    @Test
    fun scoreHabitUp() {
        val habits = tasks.filter { it.type == TaskType.HABIT }
        every { taskRepository.getTasks(TaskType.HABIT, any()) } returns Flowable.just(habits)
        fragment.taskType = TaskType.HABIT
        launchFragment()
        screen {
            recycler {
                isVisible()
                firstChild<TaskItem> {
                    // habit with only positive
                    KView(this.parent) {
                        withId(R.id.btnPlus)
                    }.click()
                    verify(exactly = 1) { taskRepository.taskChecked(any(), habits.first().id!!, true, false, any()) }
                }
            }
        }
    }

    @Test
    fun scoreHabitDown() {
        val habits = tasks.filter { it.type == TaskType.HABIT }
        val firstHabit = habits.first()
        every { taskRepository.getTasks(TaskType.HABIT, any()) } returns Flowable.just(habits)
        fragment.taskType = TaskType.HABIT
        launchFragment()
        screen {
            recycler {
                isVisible()
                childAt<TaskItem>(2) {
                    // habit with only negative
                    KView(this.parent) {
                        withId(R.id.btnMinus)
                    }.click()
                    verify(exactly = 1) { taskRepository.taskChecked(any(), firstHabit.id!!, false, false, any()) }
                }
            }
        }
    }

    @Test
    fun completeDaily() {
        val dailies = tasks.filter { it.type == TaskType.DAILY }
        every { taskRepository.getTasks(TaskType.DAILY, any()) } returns Flowable.just(dailies)
        fragment.taskType = TaskType.DAILY
        launchFragment()
        screen {
            recycler {
                isVisible()
                childAt<TaskItem>(0) {
                    KView(this.parent) {
                        withId(R.id.checkBoxHolder)
                    }.click()
                    verify(exactly = 1) { taskRepository.taskChecked(any(), dailies.first().id!!, true, false, any()) }
                }
                childAt<TaskItem>(1) {
                    KView(this.parent) {
                        withId(R.id.checkBoxHolder)
                    }.click()
                    verify(exactly = 1) { taskRepository.taskChecked(any(), dailies[1].id!!, false, false, any()) }
                }
            }
        }
    }

    @Test
    fun completeTodo() {
        val todos = tasks.filter { it.type == TaskType.TODO }
        every { taskRepository.getTasks(TaskType.TODO, any()) } returns Flowable.just(todos)
        fragment.taskType = TaskType.TODO
        launchFragment()
        screen {
            recycler {
                isVisible()
                childAt<TaskItem>(0) {
                    KView(this.parent) {
                        withId(R.id.checkBoxHolder)
                    }.click()
                    verify(exactly = 1) { taskRepository.taskChecked(any(), todos.first().id!!, true, false, any()) }
                }
            }
        }
    }
}
