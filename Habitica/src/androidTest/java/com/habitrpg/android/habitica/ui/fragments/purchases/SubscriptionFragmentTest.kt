package com.habitrpg.android.habitica.ui.fragments.purchases

/*

class SubscriptionScreen: Screen<SubscriptionScreen>() {
    val sub1MonthView = KView { withId(R.id.subscription1month) }
    val sub3MonthView = KView { withId(R.id.subscription3month) }
    val sub6MonthView = KView { withId(R.id.subscription6month) }
    val sub12MonthView = KView { withId(R.id.subscription12month) }
    val subscribeButton = KView { withId(R.id.subscribeButton) }
    val subscriptionDetails = KView { withId(R.id.subscriptionDetails) }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class SubscriptionFragmentTest :
    FragmentTestCase<SubscriptionFragment, FragmentSubscriptionBinding, SubscriptionScreen>() {
    private lateinit var subSkuMock: MockKAdditionalAnswerScope<Inventory.Product?, Inventory.Product?>
    private var purchaseHandler: PurchaseHandler = mockk(relaxed = true)

    private fun makeTestSKU(
        product: String,
        code: String,
        price: Long,
        title: String,
        description: String
    ): Sku {
        return Sku(
            product,
            code,
            "$${price}",
            Sku.Price(price, ""),
            title,
            description,
            "",
            Sku.Price(10L, ""),
            "",
            "",
            "",
            0
        )
    }

    override fun makeFragment() {
        subSkuMock = coEvery { purchaseHandler.getAllSubscriptionProducts() } answers {
            val product = mockk<Inventory.Product>()
            every { product.skus } returns listOf(
                makeTestSKU("1Month", PurchaseTypes.Subscription1Month, 99, "1 Month", "smol amount of gems"),
                makeTestSKU("3Month", PurchaseTypes.Subscription3Month, 499, "3 Months", "medium amount of gems"),
                makeTestSKU("6Month", PurchaseTypes.Subscription6Month, 999, "6 Months", "lorge amount of gems"),
                makeTestSKU("12Month", PurchaseTypes.Subscription12Month, 1999, "12 Months", "huge amount of gems")
            )
            product
        }

        scenario = launchFragmentInContainer(null, R.style.MainAppTheme) {
            fragment = spyk()
            fragment.shouldInitializeComponent = false
            fragment.userRepository = userRepository
            fragment.inventoryRepository = inventoryRepository
            fragment.tutorialRepository = tutorialRepository
            fragment.appConfigManager = appConfigManager
            fragment.setPurchaseHandler(purchaseHandler)
            return@launchFragmentInContainer fragment
        }
    }

    override val screen = SubscriptionScreen()

    @Test
    fun showsSubscriptionOptions() {
        fragment.setupCheckout()
        screen {
            sub1MonthView.isVisible()
            sub1MonthView.hasDescendant { withText("1 Month") }
            sub3MonthView.isVisible()
            sub3MonthView.hasDescendant { withText("3 Months") }
            sub6MonthView.isVisible()
            sub6MonthView.hasDescendant { withText("6 Months") }
            sub12MonthView.isVisible()
            sub12MonthView.hasDescendant { withText("12 Months") }
        }
    }
}*/
