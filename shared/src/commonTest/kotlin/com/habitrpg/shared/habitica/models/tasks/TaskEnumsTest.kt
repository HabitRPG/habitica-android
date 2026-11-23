package com.habitrpg.shared.habitica.models.tasks

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TaskEnumsTest {
    @Test
    fun taskTypeFromKnownValues() {
        TaskType.entries.forEach { assertEquals(it, TaskType.from(it.value)) }
    }

    @Test
    fun taskTypeFromUnknownOrNullOrWrongCase() {
        assertNull(TaskType.from(null))
        assertNull(TaskType.from(""))
        assertNull(TaskType.from("HABIT"))
        assertNull(TaskType.from("habits"))
    }

    @Test
    fun taskTypeToStringIsServerValue() {
        assertEquals("todo", TaskType.TODO.toString())
    }

    @Test
    fun frequencyFrom() {
        Frequency.entries.forEach { assertEquals(it, Frequency.from(it.value)) }
        assertNull(Frequency.from(null))
        assertNull(Frequency.from("Weekly"))
    }

    @Test
    fun habitResetOptionFrom() {
        assertEquals(HabitResetOption.DAILY, HabitResetOption.from(Frequency.DAILY))
        assertEquals(HabitResetOption.WEEKLY, HabitResetOption.from(Frequency.WEEKLY))
        assertEquals(HabitResetOption.MONTHLY, HabitResetOption.from(Frequency.MONTHLY))
        // habits can't reset yearly
        assertNull(HabitResetOption.from(Frequency.YEARLY))
        assertNull(HabitResetOption.from(null))
    }

    @Test
    fun attributeFromUnknown() {
        assertNull(Attribute.from(null))
        assertNull(Attribute.from("strength"))
    }

    @Test
    fun taskDifficultyRoundTrips() {
        TaskDifficulty.entries.forEach { assertEquals(it, TaskDifficulty.valueOf(it.value)) }
    }

    @Test
    fun taskDifficultyFallsBackToMediumForUnknownValues() {
        assertEquals(TaskDifficulty.MEDIUM, TaskDifficulty.valueOf(0f))
        assertEquals(TaskDifficulty.MEDIUM, TaskDifficulty.valueOf(-1f))
        assertEquals(TaskDifficulty.MEDIUM, TaskDifficulty.valueOf(3f))
        assertEquals(TaskDifficulty.MEDIUM, TaskDifficulty.valueOf(Float.NaN))
    }

    @Test
    fun taskDifficultyFromServerDouble() {
        // the server sends priority as a JSON number, which is parsed as a double first
        assertEquals(TaskDifficulty.TRIVIAL, TaskDifficulty.valueOf(0.1.toFloat()))
        assertEquals(TaskDifficulty.MEDIUM, TaskDifficulty.valueOf(1.5.toFloat()))
    }
}
