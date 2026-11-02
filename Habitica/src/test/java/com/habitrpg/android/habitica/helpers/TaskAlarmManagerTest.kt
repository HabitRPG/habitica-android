package com.habitrpg.android.habitica.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.receivers.TaskReceiver
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class TaskAlarmManagerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun scheduledAlarm(
        reminder: RemindersItem,
        occurrenceIndex: Int,
        flags: Int,
    ): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode() + occurrenceIndex,
            Intent(context, TaskReceiver::class.java).setAction(reminder.id),
            flags + PendingIntent.FLAG_IMMUTABLE,
        )

    @Test
    fun cancelsEveryUpcomingOccurrenceOfARemovedReminder() {
        val reminder = RemindersItem().apply { id = "reminder-1" }
        (0 until 3).forEach { scheduledAlarm(reminder, it, PendingIntent.FLAG_UPDATE_CURRENT).shouldNotBeNull() }

        TaskAlarmManager(context, mockk(), mockk()).cancelRemovedRemindersAlarms(listOf(reminder), emptyList())

        (0 until 3).forEach { scheduledAlarm(reminder, it, PendingIntent.FLAG_NO_CREATE).shouldBeNull() }
    }
}
