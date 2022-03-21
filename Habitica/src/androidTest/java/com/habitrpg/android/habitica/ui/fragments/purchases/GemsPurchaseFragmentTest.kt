package com.habitrpg.android.habitica.ui.fragments.purchases

/*
class GemPurchaseScreen: Screen<GemPurchaseScreen>() {
    val gems4View = KView { withId(R.id.gems_4_view) }
    val gems4Button = KTextView {
        withId(R.id.purchase_button)
        isDescendantOfA { withId(R.id.gems_4_view) }
    }
    val gems21View = KView { withId(R.id.gems_21_view) }
    val gems21Button = KTextView {
        withId(R.id.purchase_button)
        isDescendantOfA { withId(R.id.gems_21_view) }
    }
    val gems42View = KView { withId(R.id.gems_42_view) }
    val gems42Button = KTextView {
        withId(R.id.purchase_button)
        isDescendantOfA { withId(R.id.gems_42_view) }
    }
    val gems84View = KView { withId(R.id.gems_84_view) }
    val gems84Button = KTextView {
        withId(R.id.purchase_button)
        isDescendantOfA {  withId(R.id.gems_84_view) }
    }
}

@LargeTest
@RunWith(AndroidJUnit4::class)
class GemsPurchaseFragmentTest :
    FragmentTestCase<GemsPurchaseFragment, FragmentGemPurchaseBinding, GemPurchaseScreen>() {

    private lateinit var gemSkuMock: MockKAdditionalAnswerScope<List<Sku>, List<Sku>>
    private var purchaseHandler: PurchaseHandler = mockk(relaxed = true)

    override val screen = GemPurchaseScreen()

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
        gemSkuMock = coEvery { purchaseHandler.getAllGemSKUs() } returns listOf(
            makeTestSKU("4Gems", PurchaseTypes.Purchase4Gems, 99, "4 Gems", "smol amount of gems"),
            makeTestSKU("21Gems", PurchaseTypes.Purchase21Gems, 499, "21 Gems", "medium amount of gems"),
            makeTestSKU("42Gems", PurchaseTypes.Purchase42Gems, 999, "42 Gems", "lorge amount of gems"),
            makeTestSKU("84Gems", PurchaseTypes.Purchase84Gems, 1999, "84 Gems", "huge amount of gems")
        )

        scenario = launchFragmentInContainer(null, R.style.MainAppTheme) {
            fragment = spyk()
            fragment.shouldInitializeComponent = false
            fragment.userRepository = userRepository
            fragment.tutorialRepository = tutorialRepository
            fragment.appConfigManager = appConfigManager
            fragment.setPurchaseHandler(purchaseHandler)
            return@launchFragmentInContainer fragment
        }
    }

    @Test
    fun displaysGemOptions() {
        screen {
            fragment.setupCheckout()
            gems4View.hasDescendant { withText("4") }
            gems4View.hasDescendant { withText("$99") }
            gems21View.hasDescendant { withText("21") }
            gems21View.hasDescendant { withText("$499") }
            gems42View.hasDescendant { withText("42") }
            gems42View.hasDescendant { withText("$999") }
            gems84View.hasDescendant { withText("84") }
            gems84View.hasDescendant { withText("$1999") }
        }
    }

    @Test
    fun callsCorrectPurchaseFunction() {
        screen {
            fragment.setupCheckout()
            gems4Button.click()
            verify(exactly = 1) { purchaseHandler.purchaseGems(PurchaseTypes.Purchase4Gems) }

            gems21Button.click()
            verify(exactly = 1) { purchaseHandler.purchaseGems(PurchaseTypes.Purchase21Gems) }

            gems42Button.click()
            verify(exactly = 1) { purchaseHandler.purchaseGems(PurchaseTypes.Purchase42Gems) }

            gems84Button.click()
            verify(exactly = 1) { purchaseHandler.purchaseGems(PurchaseTypes.Purchase84Gems) }
        }
    }

    @Test
    fun disablesButtonsWithoutData() {
        gemSkuMock = coEvery { purchaseHandler.getAllGemSKUs() } returns emptyList()
        screen {
            fragment.setupCheckout()
            gems4Button.click()
            gems21Button.click()
            gems42Button.click()
            gems84Button.click()
            verify(exactly = 0) { purchaseHandler.purchaseGems(any()) }
        }
    }

    @Test
    fun displaysSubscriptionBannerForUnsubscribed() {
        screen {
            subscriptionPromo.isVisible()
            subscriptionPromoButton.isClickable()
        }
    }

    @Test
    fun hidesSubscriptionBannerForSubscribed() {
        user.purchased = Purchases()
        user.purchased?.plan = SubscriptionPlan()
        user.purchased?.plan?.customerId = "plan"
        userSubject.onNext(user)
        screen {
            subscriptionPromo.isGone()
        }
    }
}*/
