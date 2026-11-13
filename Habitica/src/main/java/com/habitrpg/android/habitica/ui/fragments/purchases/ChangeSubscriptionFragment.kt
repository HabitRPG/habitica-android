package com.habitrpg.android.habitica.ui.fragments.purchases

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.formattedSubscriptionPrice
import com.habitrpg.android.habitica.helpers.HabiticaProduct
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.recurranceStringRes
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaButton
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.shared.habitica.extensions.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject

private val fancyGradienColorList = listOf(Color(0xFF77F4C7), Color(0xFF72CFFF))

@Composable
fun ChangeSubscriptionOption(
    price: @Composable () -> Unit,
    recurringText: @Composable () -> Unit,
    selected: Boolean,
    isCurrentPlan: Boolean,
    modifier: Modifier = Modifier,
    benefitLine: @Composable (() -> Unit)? = null,
    bottomView: @Composable (() -> Unit)? = null,
    selectedTextColor: Color = colorResource(R.color.brand_300),
) {
    val textColor by animateColorAsState(if (selected) selectedTextColor else colorResource(R.color.brand_600))
    val backgroundColor by animateColorAsState(
        if (selected) colorResource(R.color.white) else colorResource(
            R.color.brand_200
        )
    )
    Box(
        modifier =
            modifier
                .clip(HabiticaTheme.shapes.medium)
                .background(backgroundColor)
                .fillMaxWidth(),
    ) {
        Column {
            ProvideTextStyle(
                TextStyle(
                    color = textColor,
                ),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 38.dp, vertical = 16.dp),
                ) {
                    Row {
                        ProvideTextStyle(
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            ),
                        ) {
                            price()
                        }
                    }
                    ProvideTextStyle(
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                    ) {
                        recurringText()
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.sub_plus),
                            colorFilter = ColorFilter.tint(colorResource(if (selected) R.color.yellow_100 else R.color.brand_400)),
                            contentDescription = null,
                        )
                        ProvideTextStyle(
                            TextStyle(
                                fontSize = 15.sp,
                            ),
                        ) {
                            if (benefitLine != null) {
                                benefitLine()
                            } else {
                                Text(stringResource(R.string.continue_current_benefits))
                            }
                        }
                    }
                }
            }
            bottomView?.invoke()
        }
        if (isCurrentPlan) {
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.TopEnd)
            ) {
                Image(
                    painterResource(R.drawable.flag_flap),
                    contentDescription = null,
                )
                Text(
                    stringResource(R.string.current_plan),
                    color = colorResource(R.color.teal_1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier
                            .background(Brush.horizontalGradient(fancyGradienColorList))
                            .height(24.dp)
                            .padding(horizontal = 8.dp)
                            .wrapContentHeight(),
                )
            }
        }
        AnimatedVisibility(
            selected,
            enter =
                slideInHorizontally(
                    tween(
                        durationMillis = 600,
                        delayMillis = 50,
                        easing = EaseInElastic
                    )
                ) {
                    -it
                } + fadeIn(tween(durationMillis = 600, delayMillis = 50, easing = EaseInElastic)),
            exit = slideOutHorizontally { -it } + fadeOut(),
        ) {
            Image(
                painterResource(R.drawable.subscription_selected_indicator),
                contentDescription = null,
            )
        }
    }
}

@HiltViewModel
class ChangeSubscriptionViewModel
@Inject
constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    private val purchaseHandler: PurchaseHandler,
) : BaseViewModel(userRepository, userViewModel) {
    fun selectProduct(product: HabiticaProduct) {
        selectedProduct.value = product
    }

    val isDowngrade: Boolean
        get() =
            currentProduct.value != null &&
                    selectedProduct.value.getSubscriptionDuration() <= (currentProduct.value?.getSubscriptionDuration()
                ?: 0)
    val products =
        listOf(
            HabiticaProduct.SUBSCRIPTION_1_MONTH,
            HabiticaProduct.SUBSCRIPTION_3_MONTH,
            HabiticaProduct.SUBSCRIPTION_6_MONTH,
            HabiticaProduct.SUBSCRIPTION_12_MONTH,
        )

    val currentStep = MutableStateFlow(0)

    val activeSubscriptionPlan = userViewModel.user.map { it?.purchased?.plan }
    val newestSubscription = MutableStateFlow<Purchase?>(null)
    val currentProduct = MutableStateFlow<HabiticaProduct?>(null)
    val selectedProduct = MutableStateFlow(HabiticaProduct.SUBSCRIPTION_1_MONTH)

    val productDetails = MutableStateFlow<Map<HabiticaProduct, ProductDetails>>(emptyMap())

    // Kept up to date by the collector in init, so these don't depend on activeSubscriptionPlan being observed
    private var latestPlan: SubscriptionPlan? = null

    val isEligibleForHourglassPromo: Boolean
        get() = latestPlan?.isEligableForHourglassPromo == true
    val hadGiftedSubscription: Boolean
        get() = latestPlan?.isGiftedSub == true
    val totalGemCount: Int
        get() = latestPlan?.totalNumberOfGems ?: 0

    init {
        viewModelScope.launchCatching {
            userViewModel.user.asFlow().collect { user ->
                latestPlan = user?.purchased?.plan
                // Gifted and expired plans can't be changed, so there is no current plan to preselect
                val product =
                    latestPlan
                        ?.takeIf { !it.isGiftedSub && !it.isPaymentExpired }
                        ?.habiticaProduct
                // Only reset the selection when the plan changes, not on every user update
                if (product != currentProduct.value) {
                    currentProduct.value = product
                    if (product != null) {
                        selectedProduct.value = product
                    }
                }
            }
        }

        viewModelScope.launchCatching {
            val details = HashMap<HabiticaProduct, ProductDetails>()
            val products = purchaseHandler.loadSubscriptionProducts()
            for (product in products) {
                val habiticaProduct = HabiticaProduct.forSku(product.productId)
                if (habiticaProduct != null) {
                    details[habiticaProduct] = product
                }
            }
            productDetails.value = details
        }

        viewModelScope.launchCatching {
            newestSubscription.value = purchaseHandler.checkForSubscription(false)
        }
    }

    fun productDetailsForProduct(product: HabiticaProduct): ProductDetails? =
        productDetails.value[product]

    fun purchaseSubscription(
        activity: Activity,
        onPurchaseStarted: () -> Unit,
    ) {
        val details = productDetailsForProduct(selectedProduct.value) ?: return
        viewModelScope.launchCatching {
            purchaseHandler.purchase(activity, details)
            onPurchaseStarted()
        }
    }

    fun nextStep() {
        currentStep.value += 1
    }

    fun estimatedYearlyPrice(): String {
        val details = productDetailsForProduct(HabiticaProduct.SUBSCRIPTION_1_MONTH) ?: return ""
        val monthlyPricePhase =
            details.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull { it.priceAmountMicros > 0 } ?: return ""
        var yearlyPrice = (monthlyPricePhase.priceAmountMicros * 12).div(1_000_000.0)
        yearlyPrice = yearlyPrice.round(0) - 0.01
        // Let the platform place the symbol and separators correctly for the user's locale
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(monthlyPricePhase.priceCurrencyCode)
        return format.format(yearlyPrice)
    }
}

@Composable
private fun ChangeSubscriptionChoiceView(
    modifier: Modifier = Modifier,
    viewModel: ChangeSubscriptionViewModel,
) {
    val currentPlan by viewModel.currentProduct.collectAsStateWithLifecycle(null)
    val selectedSub by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val currentSubscription by viewModel.activeSubscriptionPlan.observeAsState()
    val canContinue = selectedSub != currentPlan || currentSubscription?.dateTerminated != null
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HabiticaButton(
            colorResource(R.color.yellow_100),
            colorResource(R.color.brand_100),
            {
                viewModel.nextStep()
            },
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier
                .padding(top = 20.dp)
                .alpha(if (canContinue) 1f else 0.5f),
            enabled = canContinue,
        ) {
            Text(stringResource(R.string.action_continue))
        }
        Text(
            stringResource(R.string.review_subscription_change_info),
            fontStyle = FontStyle.Italic,
            color = colorResource(R.color.white),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 26.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun ChangeSubscriptionReviewView(
    viewModel: ChangeSubscriptionViewModel,
    onPurchaseStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val activePlan by viewModel.activeSubscriptionPlan.observeAsState()
    val newestSub by viewModel.newestSubscription.collectAsState()
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(top = 20.dp),
    ) {
        ProvideTextStyle(
            TextStyle(
                color = colorResource(R.color.white),
                fontSize = 14.sp,
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (activePlan?.isTerminated == true && newestSub?.purchaseToken == activePlan?.customerId) {
                    if (viewModel.isDowngrade) {
                        Text(
                            stringResource(R.string.subscription_change_confirmation_reactivate_downgrade),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            stringResource(R.string.subscription_change_confirmation_reactivate_upgrade),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        )
                    }
                } else if (viewModel.hadGiftedSubscription || activePlan?.isTerminated == true && newestSub == null) {
                    val duration = selectedProduct.getSubscriptionDuration()
                    Text(
                        pluralStringResource(
                            R.plurals.subscription_change_confirmation_gift,
                            duration,
                            duration
                        ),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        fontSize = 16.sp
                    )
                    Text(
                        stringResource(R.string.subscription_change_gift_info),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                } else {
                    if (viewModel.isDowngrade) {
                        Text(
                            stringResource(R.string.subscription_change_confirmation_downgrade),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            stringResource(R.string.subscription_change_confirmation_upgrade),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            fontSize = 16.sp
                        )
                    }
                }
                if (selectedProduct == HabiticaProduct.SUBSCRIPTION_12_MONTH) {
                    if (viewModel.isEligibleForHourglassPromo && viewModel.totalGemCount < 50) {
                        Text(
                            stringResource(R.string.subscription_change_hourglass_promo_gems_info),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    } else if (viewModel.isEligibleForHourglassPromo) {
                        Text(
                            stringResource(R.string.subscription_change_hourglass_promo_info),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
        val activity = LocalActivity.current
        HabiticaButton(
            colorResource(R.color.yellow_100),
            colorResource(R.color.brand_100),
            {
                activity?.let { viewModel.purchaseSubscription(it, onPurchaseStarted) }
            },
            contentPadding = PaddingValues(15.dp),
        ) {
            Text(stringResource(R.string.complete_purchase))
        }
    }
}

@Composable
fun ChangeSubscriptionScreen(
    dismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangeSubscriptionViewModel = sheetScopedViewModel(),
) {
    val step by viewModel.currentStep.collectAsStateWithLifecycle()
    val activeSub by viewModel.activeSubscriptionPlan.observeAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize(),
        ) {
            Text(
                stringResource(R.string.change_subscription_plan),
                color = colorResource(R.color.white),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 20.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colorResource(R.color.brand_400))
                ) {}
                Image(painterResource(R.drawable.separator_fancy), contentDescription = null)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(colorResource(R.color.brand_400))
                ) {}
            }

            val currentPlan by viewModel.currentProduct.collectAsStateWithLifecycle(null)
            val selectedSub by viewModel.selectedProduct.collectAsStateWithLifecycle()
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.animateContentSize()
            ) {
                val details by viewModel.productDetails.collectAsStateWithLifecycle()
                for (product in viewModel.products) {
                    AnimatedVisibility(step == 0 || product == selectedSub) {
                        if (product == HabiticaProduct.SUBSCRIPTION_12_MONTH) {
                            ChangeSubscriptionOption(
                                price = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            details[product]?.formattedSubscriptionPrice ?: "",
                                            style =
                                                TextStyle(
                                                    brush = Brush.horizontalGradient(
                                                        fancyGradienColorList
                                                    ),
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                        )
                                        Text(
                                            viewModel.estimatedYearlyPrice(),
                                            textDecoration = TextDecoration.LineThrough,
                                            color = colorResource(if (selectedSub == product) R.color.gray_400 else R.color.brand_600),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                },
                                recurringText = {
                                    Text(
                                        stringResource(
                                            R.string.subscription_duration,
                                            stringResource(product.recurranceStringRes)
                                        ),
                                    )
                                },
                                benefitLine =
                                    if (activeSub?.totalNumberOfGems != 50) {
                                        {
                                            Text(stringResource(R.string.raises_gem_cap_text))
                                        }
                                    } else {
                                        null
                                    },
                                bottomView =
                                    if (activeSub?.isEligableForHourglassPromo == true) {
                                        {
                                            Text(
                                                stringResource(R.string.get_12_mystic_hourglasses),
                                                color = colorResource(R.color.teal_1),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center,
                                                modifier =
                                                    Modifier
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                fancyGradienColorList
                                                            )
                                                        )
                                                        .padding(
                                                            horizontal = 24.dp,
                                                            vertical = 12.dp
                                                        ),
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                selected = selectedSub == product,
                                isCurrentPlan = currentPlan == product,
                                selectedTextColor = colorResource(R.color.teal_1),
                                modifier =
                                    Modifier.clickable {
                                        viewModel.selectProduct(product)
                                    },
                            )
                        } else {
                            ChangeSubscriptionOption(
                                price = {
                                    Text(
                                        details[product]?.formattedSubscriptionPrice ?: ""
                                    )
                                },
                                recurringText = {
                                    Text(
                                        stringResource(
                                            R.string.subscription_duration,
                                            stringResource(product.recurranceStringRes)
                                        ),
                                    )
                                },
                                selected = selectedSub == product,
                                isCurrentPlan = currentPlan == product,
                                modifier =
                                    Modifier.clickable {
                                        viewModel.selectProduct(product)
                                    },
                            )
                        }
                    }
                }
            }
            AnimatedContent(step) {
                when (it) {
                    0 -> ChangeSubscriptionChoiceView(viewModel = viewModel)
                    else -> ChangeSubscriptionReviewView(viewModel = viewModel, onPurchaseStarted = dismiss)
                }
            }
        }
        Image(
            painterResource(R.drawable.footer_hills),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(
                if (step == 0) {
                    R.string.subscriptions_renew_info
                } else if (viewModel.isDowngrade) {
                    R.string.subscription_renew_review_info_downgrade
                } else {
                    R.string.subscription_renew_review_info
                },
            ),
            color = colorResource(R.color.white),
            fontSize = 12.sp,
            modifier =
                Modifier
                    .background(colorResource(R.color.brand_400))
                    .animateContentSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 20.dp),
        )
    }
}

/**
 * The sheet is a ComposeView added to the activity, so viewModel() would return an activity-scoped instance that is
 * reused (with stale state) every time the sheet is opened. This scopes the ViewModel to the sheet's composition
 * instead, while still creating it through the activity's Hilt factory.
 */
@Composable
private inline fun <reified VM : ViewModel> sheetScopedViewModel(): VM {
    val activity = requireNotNull(LocalActivity.current as? ComponentActivity)
    val owner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    val extras =
        remember(owner) {
            MutableCreationExtras(activity.defaultViewModelCreationExtras).apply {
                set(VIEW_MODEL_STORE_OWNER_KEY, owner)
            }
        }
    return viewModel(
        viewModelStoreOwner = owner,
        factory = activity.defaultViewModelProviderFactory,
        extras = extras,
    )
}
