package com.habitrpg.android.habitica.helpers

import android.content.SharedPreferences
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.WorldStateEvent
import com.habitrpg.android.habitica.models.promotions.FallExtraGemsHabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.GiftOneGetOneHabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.SpringExtraGemsHabiticaPromotion
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Date
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class AppConfigManagerTest :
    WordSpec({
        val remoteConfig = mockk<RemoteConfig>(relaxed = true)
        val sharedPreferences = mockk<SharedPreferences>()
        val contentRepository = mockk<ContentRepository>()
        val worldState = MutableStateFlow(WorldState())
        lateinit var configManager: AppConfigManager

        val now = Date().time
        val past = Date(now - 2.days.inWholeMilliseconds)
        val future = Date(now + 2.days.inWholeMilliseconds)

        fun event(
            key: String?,
            promo: String? = null,
            start: Date? = past,
            end: Date? = future,
        ) = WorldStateEvent().apply {
            eventKey = key
            this.promo = promo
            this.start = start
            this.end = end
        }

        fun publishWorldState(
            currentEvent: WorldStateEvent? = null,
            vararg events: WorldStateEvent,
        ) {
            worldState.value =
                WorldState().apply {
                    this.currentEvent = currentEvent
                    this.events.addAll(events)
                }
        }

        beforeEach {
            Dispatchers.setMain(UnconfinedTestDispatcher())
            worldState.value = WorldState()
            every { contentRepository.getWorldState() } returns worldState
            every { sharedPreferences.getString("active_promo", null) } returns null
            configManager = AppConfigManager({ contentRepository }, sharedPreferences, remoteConfig)
        }
        afterEach { Dispatchers.resetMain() }

        "remote config values" should {
            "be read from firebase" {
                every { remoteConfig.getBoolean("enableLocalTaskScoring") } returns true
                every { remoteConfig.getLong("maxChatLength") } returns 3000
                every { remoteConfig.getString("supportEmail") } returns "admin@habitica.com"
                configManager.enableLocalTaskScoring() shouldBe true
                configManager.maxChatLength() shouldBe 3000
                configManager.supportEmail() shouldBe "admin@habitica.com"
            }

            "parse known issues" {
                every { remoteConfig.getString("knownIssues") } returns """[{"title":"Sync","text":"Tasks may not sync"}]"""
                configManager.knownIssues() shouldBe listOf(mapOf("title" to "Sync", "text" to "Tasks may not sync"))
            }

            "parse an empty list of known issues" {
                every { remoteConfig.getString("knownIssues") } returns "[]"
                configManager.knownIssues().shouldBeEmpty()
            }
        }

        "enableTaskDisplayMode" should {
            "always be enabled in debug builds" {
                every { remoteConfig.getBoolean("enableTaskDisplayMode") } returns false
                configManager.enableTaskDisplayMode() shouldBe BuildConfig.DEBUG
            }
        }

        "shopSpriteSuffix" should {
            "come from the world state" {
                publishWorldState(WorldStateEvent().apply { npcImageSuffix = "spring" })
                configManager.shopSpriteSuffix() shouldBe "spring"
            }
        }

        "activePromo" should {
            "be null without any events".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                configManager.activePromo().shouldBeNull()
            }

            "use the promo key of the current event".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                publishWorldState(event("spring2026", promo = "spring_extra_gems"))
                val promo = configManager.activePromo().shouldBeInstanceOf<SpringExtraGemsHabiticaPromotion>()
                promo.startDate shouldBe past
                promo.endDate shouldBe future
            }

            "fall back to the event key".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                publishWorldState(event("g1g1"))
                configManager.activePromo().shouldBeInstanceOf<GiftOneGetOneHabiticaPromotion>()
            }

            "find promos in the event list".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                publishWorldState(null, event("birthday10"), event("fall2020"))
                configManager.activePromo().shouldBeInstanceOf<FallExtraGemsHabiticaPromotion>()
            }

            "prefer the current event over the event list".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                publishWorldState(event("g1g1"), event("spring_extra_gems"))
                configManager.activePromo().shouldBeInstanceOf<GiftOneGetOneHabiticaPromotion>()
            }

            "ignore promos that have not started yet".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                publishWorldState(event("spring_extra_gems", start = future, end = Date(future.time + 1.days.inWholeMilliseconds)))
                configManager.activePromo().shouldBeNull()
            }

            "use the promo set in the debug settings".config(enabled = BuildConfig.DEBUG) {
                every { sharedPreferences.getString("active_promo", null) } returns "fall_extra_gems"
                configManager.activePromo().shouldBeInstanceOf<FallExtraGemsHabiticaPromotion>()
            }

            "ignore a blank promo in the debug settings".config(enabled = BuildConfig.ACTIVE_PROMO.isBlank()) {
                every { sharedPreferences.getString("active_promo", null) } returns " "
                configManager.activePromo().shouldBeNull()
            }
        }

        "getBirthdayEvent" should {
            "return the running birthday event" {
                val birthday = event("birthday10")
                publishWorldState(null, event("spring_extra_gems"), birthday)
                configManager.getBirthdayEvent() shouldBe birthday
            }

            "ignore a birthday event that is over" {
                publishWorldState(null, event("birthday10", end = past))
                configManager.getBirthdayEvent().shouldBeNull()
            }

            "be null without a birthday event" {
                publishWorldState(event("spring_extra_gems"))
                configManager.getBirthdayEvent().shouldBeNull()
            }
        }
    })
