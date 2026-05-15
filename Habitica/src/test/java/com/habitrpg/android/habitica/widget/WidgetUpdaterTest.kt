package com.habitrpg.android.habitica.widget

import android.content.Context
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.clearAllMocks
import io.mockk.mockk

class WidgetUpdaterTest : WordSpec({
    val context = mockk<Context>(relaxed = true)
    val refreshes = mutableListOf<Context>()

    beforeEach {
        refreshes.clear()
    }

    fun testWidgetUpdater(): WidgetUpdater {
        return WidgetUpdater(
            context,
            refreshScheduler = { refreshes.add(it) }
        )
    }

    "updateTaskListWidgets" should {
        "schedule a local Glance widget refresh" {
            testWidgetUpdater().updateTaskListWidgets()

            refreshes shouldContainExactly listOf(context)
        }
    }

    "updateAllWidgets" should {
        "schedule a local Glance widget refresh" {
            testWidgetUpdater().updateAllWidgets()

            refreshes shouldContainExactly listOf(context)
        }
    }

    afterEach {
        clearAllMocks()
    }
})
