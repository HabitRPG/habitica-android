package com.habitrpg.android.habitica.models.promotions

import com.habitrpg.android.habitica.BuildConfig
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date
import kotlin.time.Duration.Companion.days

class HabiticaPromotionTest :
    WordSpec({
        val now = Date().time
        val past = Date(now - 2.days.inWholeMilliseconds)
        val future = Date(now + 2.days.inWholeMilliseconds)

        "getHabiticaPromotionFromKey" should {
            "map the seasonal gem sales" {
                getHabiticaPromotionFromKey("spring_extra_gems", null, null).shouldBeInstanceOf<SpringExtraGemsHabiticaPromotion>()
                getHabiticaPromotionFromKey("summer_extra_gems", null, null).shouldBeInstanceOf<SummerExtraGemsHabiticaPromotion>()
                getHabiticaPromotionFromKey("winter_extra_gems", null, null).shouldBeInstanceOf<WinterExtraGemsHabiticaPromotion>()
                getHabiticaPromotionFromKey("flash_extra_gems", null, null).shouldBeInstanceOf<FlashExtraGemsHabiticaPromotion>()
            }

            "map legacy fall promo keys" {
                listOf("fall_extra_gems", "fall2020", "testFall2020").forEach {
                    getHabiticaPromotionFromKey(it, null, null).shouldBeInstanceOf<FallExtraGemsHabiticaPromotion>()
                }
                listOf("spooky_extra_gems", "fall2020SecondPromo", "spooky2020").forEach {
                    getHabiticaPromotionFromKey(it, null, null).shouldBeInstanceOf<SpookyExtraGemsHabiticaPromotion>()
                }
            }

            "map gift one get one" {
                getHabiticaPromotionFromKey("g1g1", null, null).shouldBeInstanceOf<GiftOneGetOneHabiticaPromotion>()
            }

            "map the survey" {
                getHabiticaPromotionFromKey("survey2021", null, null).shouldBeInstanceOf<Survey2021Promotion>()
            }

            "return null for unknown keys" {
                getHabiticaPromotionFromKey("birthday10", null, null).shouldBeNull()
                getHabiticaPromotionFromKey("", null, null).shouldBeNull()
            }

            "use the given dates" {
                val promo = getHabiticaPromotionFromKey("spring_extra_gems", past, future).shouldNotBeNull()
                promo.startDate shouldBe past
                promo.endDate shouldBe future
            }

            "have an identifier matching the canonical key" {
                listOf("spring_extra_gems", "summer_extra_gems", "fall_extra_gems", "spooky_extra_gems", "winter_extra_gems", "flash_extra_gems", "g1g1", "survey2021")
                    .forEach { key -> getHabiticaPromotionFromKey(key, null, null)?.identifier shouldBe key }
            }
        }

        "promoType" should {
            "be a gem amount promo for gem sales" {
                getHabiticaPromotionFromKey("spring_extra_gems", null, null)?.promoType shouldBe PromoType.GEMS_AMOUNT
            }

            "be a subscription promo for gift one get one" {
                getHabiticaPromotionFromKey("g1g1", null, null)?.promoType shouldBe PromoType.SUBSCRIPTION
            }

            "be a survey promo for surveys" {
                getHabiticaPromotionFromKey("survey2021", null, null)?.promoType shouldBe PromoType.SURVEY
            }
        }

        "isActive" should {
            "be true while the promo is running" {
                SpringExtraGemsHabiticaPromotion(past, future).isActive shouldBe true
            }

            "be false before the promo started" {
                SpringExtraGemsHabiticaPromotion(future, Date(future.time + 1.days.inWholeMilliseconds)).isActive shouldBe false
            }

            "be false after the promo ended in release builds".config(enabled = !BuildConfig.DEBUG && BuildConfig.TESTING_LEVEL != "staff") {
                SpringExtraGemsHabiticaPromotion(Date(past.time - 1.days.inWholeMilliseconds), past).isActive shouldBe false
            }

            "stay active after the end date for debug and staff builds".config(enabled = BuildConfig.DEBUG || BuildConfig.TESTING_LEVEL == "staff") {
                SpringExtraGemsHabiticaPromotion(Date(past.time - 1.days.inWholeMilliseconds), past).isActive shouldBe true
            }
        }
    })
