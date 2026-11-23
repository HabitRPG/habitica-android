package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class ReviewManagerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs = context.getSharedPreferences("ReviewPrefs", Context.MODE_PRIVATE)
    private val config = mutableMapOf<ClientConfigKey<*>, Any>()
    private lateinit var reviewManager: ReviewManager

    @Before
    fun setUp() {
        val configManager = mockk<AppConfigManager>()
        every { configManager.value(any<ClientConfigKey<Any>>()) } answers {
            val key = firstArg<ClientConfigKey<Any>>()
            config[key] ?: key.defaultValue
        }
        reviewManager = ReviewManager(context, configManager)
    }

    private fun history(
        initial: Int,
        lastRequest: Int = -1,
        requests: Int = 0,
    ) = prefs.edit {
        putInt("InitialCheckins", initial)
        putInt("LastReviewCheckin", lastRequest)
        putInt("ReviewRequestCount", requests)
    }

    @Test
    fun usesTheDefaultLimits() {
        history(initial = 10, lastRequest = 10, requests = 4)
        reviewManager.canRequestReview(14) shouldBe false
        reviewManager.canRequestReview(15) shouldBe true
        reviewManager.canRequestReview(86) shouldBe false
        history(initial = 10, requests = 5)
        reviewManager.canRequestReview(20) shouldBe false
    }

    @Test
    fun followsTheMaxRequests() {
        config[ClientConfigKey.reviewMaxRequests] = 8L
        history(initial = 10, requests = 5)
        reviewManager.canRequestReview(20) shouldBe true
        config[ClientConfigKey.reviewMaxRequests] = 2L
        history(initial = 10, requests = 2)
        reviewManager.canRequestReview(20) shouldBe false
    }

    @Test
    fun followsTheCheckinWindow() {
        config[ClientConfigKey.reviewMaxCheckinsSinceFirstRequest] = 20L
        history(initial = 10)
        reviewManager.canRequestReview(30) shouldBe true
        reviewManager.canRequestReview(31) shouldBe false
    }

    @Test
    fun followsTheGapBetweenRequests() {
        config[ClientConfigKey.reviewMinCheckinsBetweenRequests] = 10L
        history(initial = 10, lastRequest = 20)
        reviewManager.canRequestReview(29) shouldBe false
        reviewManager.canRequestReview(30) shouldBe true
    }

    @Test
    fun followsTheMinimumCheckins() {
        config[ClientConfigKey.reviewCheckingMinCount] = 50L
        history(initial = 10)
        reviewManager.canRequestReview(49) shouldBe false
        reviewManager.canRequestReview(50) shouldBe true
    }
}
