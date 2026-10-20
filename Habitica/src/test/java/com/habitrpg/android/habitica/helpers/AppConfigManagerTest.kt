package com.habitrpg.android.habitica.helpers

import android.content.SharedPreferences
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
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
            clearMocks(contentRepository, sharedPreferences)
            Dispatchers.setMain(UnconfinedTestDispatcher())
            worldState.value = WorldState()
            every { contentRepository.getWorldState() } returns worldState
            every { sharedPreferences.getString("active_promo", null) } returns null
            every { sharedPreferences.getString("clientConfig", null) } returns null
            configManager = AppConfigManager({ contentRepository }, sharedPreferences)
        }
        afterEach { Dispatchers.resetMain() }

        "value" should {
            "return the default without a loaded config" {
                configManager.value(ClientConfigKey.maxChatLength) shouldBe 3000L
                configManager.value(ClientConfigKey.supportEmail) shouldBe "admin@habitica.com"
            }

            "return the loaded value" {
                configManager.updateConfig(
                    JsonParser.parseString("""{"maxChatLength":500,"hideChallenges":true,"supportEmail":"a@b.c"}""").asJsonObject,
                )
                configManager.value(ClientConfigKey.maxChatLength) shouldBe 500L
                configManager.value(ClientConfigKey.hideChallenges) shouldBe true
                configManager.value(ClientConfigKey.supportEmail) shouldBe "a@b.c"
            }

            "parse known issues" {
                configManager.updateConfig(
                    JsonParser.parseString("""{"knownIssues":[{"title":"Sync","text":"Tasks may not sync"}]}""").asJsonObject,
                )
                configManager.value(ClientConfigKey.knownIssues) shouldBe listOf(mapOf("title" to "Sync", "text" to "Tasks may not sync"))
            }

            "fall back to the default for values of the wrong type" {
                configManager.updateConfig(
                    JsonParser.parseString("""{"maxChatLength":"many","knownIssues":"none","lastVersionCode":null}""").asJsonObject,
                )
                configManager.value(ClientConfigKey.maxChatLength) shouldBe 3000L
                configManager.value(ClientConfigKey.knownIssues).shouldBeEmpty()
                configManager.value(ClientConfigKey.lastVersionCode) shouldBe 0L
            }

            "use the config stored by a previous launch" {
                every { sharedPreferences.getString("clientConfig", null) } returns """{"maxChatLength":42}"""
                AppConfigManager({ contentRepository }, sharedPreferences).value(ClientConfigKey.maxChatLength) shouldBe 42L
            }

            "ignore a corrupted stored config" {
                every { sharedPreferences.getString("clientConfig", null) } returns "{not json"
                AppConfigManager({ contentRepository }, sharedPreferences).value(ClientConfigKey.maxChatLength) shouldBe 3000L
            }

            "store the loaded config" {
                val editor = mockk<SharedPreferences.Editor>(relaxed = true)
                every { sharedPreferences.edit() } returns editor
                configManager.updateConfig(JsonParser.parseString("""{"maxChatLength":42}""").asJsonObject)
                verify { editor.putString("clientConfig", """{"maxChatLength":42}""") }
                verify { editor.apply() }
            }
        }

        "fetchConfig" should {
            "update the config from the server" {
                coEvery { contentRepository.retrieveClientConfig() } returns
                    JsonParser.parseString("""{"maxChatLength":10}""").asJsonObject
                configManager.fetchConfig()
                configManager.value(ClientConfigKey.maxChatLength) shouldBe 10L
            }

            "only load once per day unless forced" {
                coEvery { contentRepository.retrieveClientConfig() } returns JsonObject()
                configManager.fetchConfig()
                configManager.fetchConfig()
                coVerify(exactly = 1) { contentRepository.retrieveClientConfig() }
                configManager.fetchConfig(forced = true)
                coVerify(exactly = 2) { contentRepository.retrieveClientConfig() }
            }

            "keep the current config when loading fails" {
                configManager.updateConfig(JsonParser.parseString("""{"maxChatLength":10}""").asJsonObject)
                coEvery { contentRepository.retrieveClientConfig() } returns null
                configManager.fetchConfig(forced = true)
                configManager.value(ClientConfigKey.maxChatLength) shouldBe 10L
            }
        }

        "enableTaskDisplayMode" should {
            "always be enabled in debug builds" {
                configManager.updateConfig(JsonParser.parseString("""{"enableTaskDisplayMode":false}""").asJsonObject)
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
