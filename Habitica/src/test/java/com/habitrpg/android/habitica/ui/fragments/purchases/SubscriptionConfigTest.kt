package com.habitrpg.android.habitica.ui.fragments.purchases

import android.os.Looper
import android.view.View
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.HabiticaProduct
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.testing.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-rUS")
class SubscriptionConfigTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private val products =
        listOf(
            HabiticaProduct.SUBSCRIPTION_1_MONTH to 5_000_000L,
            HabiticaProduct.SUBSCRIPTION_3_MONTH to 15_000_000L,
            HabiticaProduct.SUBSCRIPTION_6_MONTH to 30_000_000L,
            HabiticaProduct.SUBSCRIPTION_12_MONTH to 50_000_000L,
        ).map { (product, micros) -> product(product.sku, micros) }

    @Before
    fun setUp() {
        hiltRule.inject()
        coEvery { purchaseHandler.loadSubscriptionProducts() } returns products
    }

    private fun product(
        sku: String,
        micros: Long,
    ): ProductDetails {
        val phase =
            mockk<ProductDetails.PricingPhase> {
                every { priceAmountMicros } returns micros
                every { formattedPrice } returns "$micros"
            }
        val offer =
            mockk<ProductDetails.SubscriptionOfferDetails>(relaxed = true) {
                every { offerId } returns null
                every { pricingPhases.pricingPhaseList } returns listOf(phase)
            }
        return mockk(relaxed = true) {
            every { productId } returns sku
            every { subscriptionOfferDetails } returns listOf(offer)
        }
    }

    private fun config(json: String) = configManager.updateConfig(Json.parseToJsonElement(json).jsonObject)

    private fun withSubscriptionScreen(block: SubscriptionFragment.() -> Unit) {
        launchFragmentInHiltContainer<SubscriptionFragment> {
            shadowOf(Looper.getMainLooper()).idle()
            block()
        }.close()
    }

    private fun SubscriptionFragment.flaggedSkus() =
        listOfNotNull(content?.subscription1month, content?.subscription3month, content?.subscription6month, content?.subscription12month)
            .filter { it.findViewById<View>(R.id.flag_textview).visibility == View.VISIBLE }
            .map { it.sku }

    @Test
    fun selectsAndHighlightsTheYearlyPlanByDefault() =
        withSubscriptionScreen {
            selectedSubscriptionSku?.productId shouldBe HabiticaProduct.SUBSCRIPTION_12_MONTH.sku
            flaggedSkus() shouldBe listOf(HabiticaProduct.SUBSCRIPTION_12_MONTH.sku)
        }

    @Test
    fun selectsTheConfiguredPlan() {
        config("""{"defaultSubscriptionSku":"${HabiticaProduct.SUBSCRIPTION_1_MONTH.sku}"}""")
        withSubscriptionScreen {
            selectedSubscriptionSku?.productId shouldBe HabiticaProduct.SUBSCRIPTION_1_MONTH.sku
        }
    }

    @Test
    fun fallsBackToTheMostExpensivePlanForAnUnknownSku() {
        config("""{"defaultSubscriptionSku":"not.a.sku"}""")
        withSubscriptionScreen {
            selectedSubscriptionSku?.productId shouldBe HabiticaProduct.SUBSCRIPTION_12_MONTH.sku
        }
    }

    @Test
    fun highlightsTheConfiguredPlan() {
        config("""{"highlightedSubscriptionSku":"${HabiticaProduct.SUBSCRIPTION_6_MONTH.sku}"}""")
        withSubscriptionScreen {
            flaggedSkus() shouldBe listOf(HabiticaProduct.SUBSCRIPTION_6_MONTH.sku)
        }
    }

    @Test
    fun highlightsNothingForABlankSku() {
        config("""{"highlightedSubscriptionSku":""}""")
        withSubscriptionScreen {
            flaggedSkus() shouldBe emptyList()
        }
    }
}
