package com.habitrpg.shared.habitica.models.tasks

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeTask(
    override var completed: Boolean = false,
    override var type: TaskType? = null,
    override var isDue: Boolean? = null,
    override var streak: Int? = null,
    override var counterDown: Int? = null,
    override var counterUp: Int? = null,
) : BaseTask

class BaseTaskTest {
    @Test
    fun streakStringShowsBothCounters() {
        assertEquals("+3 | -2", FakeTask(counterUp = 3, counterDown = 2).streakString)
    }

    @Test
    fun streakStringShowsSingleCounter() {
        assertEquals("+3", FakeTask(counterUp = 3, counterDown = 0).streakString)
        assertEquals("-2", FakeTask(counterUp = null, counterDown = 2).streakString)
    }

    @Test
    fun countersTakePrecedenceOverStreak() {
        assertEquals("+1", FakeTask(counterUp = 1, streak = 10).streakString)
    }

    @Test
    fun streakStringFallsBackToStreak() {
        assertEquals("10", FakeTask(streak = 10).streakString)
    }

    @Test
    fun streakStringIsNullWhenNothingToShow() {
        assertNull(FakeTask().streakString)
        assertNull(FakeTask(counterUp = 0, counterDown = 0, streak = 0).streakString)
        assertNull(FakeTask(counterUp = -1, counterDown = -1, streak = -5).streakString)
    }

    @Test
    fun isDisplayedActiveForDueIncompleteDaily() {
        assertTrue(FakeTask(type = TaskType.DAILY, isDue = true).isDisplayedActive)
        assertFalse(FakeTask(type = TaskType.DAILY, isDue = false).isDisplayedActive)
        assertFalse(FakeTask(type = TaskType.DAILY, isDue = null).isDisplayedActive)
        assertFalse(FakeTask(type = TaskType.DAILY, isDue = true, completed = true).isDisplayedActive)
    }

    @Test
    fun isDisplayedActiveForTodoIgnoresIsDue() {
        assertTrue(FakeTask(type = TaskType.TODO, isDue = false).isDisplayedActive)
        assertFalse(FakeTask(type = TaskType.TODO, completed = true).isDisplayedActive)
    }

    @Test
    fun habitsRewardsAndUntypedAreNeverDisplayedActive() {
        assertFalse(FakeTask(type = TaskType.HABIT, isDue = true).isDisplayedActive)
        assertFalse(FakeTask(type = TaskType.REWARD, isDue = true).isDisplayedActive)
        assertFalse(FakeTask(type = null, isDue = true).isDisplayedActive)
    }
}
