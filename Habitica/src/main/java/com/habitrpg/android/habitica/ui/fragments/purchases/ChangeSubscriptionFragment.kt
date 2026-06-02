package com.habitrpg.android.habitica.ui.fragments.purchases

import android.app.Activity
import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.billingclient.api.ProductDetails
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
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
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.RoundingMode
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
    selectedTextColor: Color = colorResource(R.color.brand_300)) {
    val textColor by animateColorAsState(if (selected) selectedTextColor else colorResource(R.color.brand_600))
    val backgroundColor by animateColorAsState(if (selected) colorResource(R.color.white) else colorResource(R.color.brand_200))
    Box(modifier = modifier
        .clip(HabiticaTheme.shapes.medium)
        .background(backgroundColor)
        .fillMaxWidth()) {
        Column {
            ProvideTextStyle(
                TextStyle(
                    color = textColor
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(horizontal = 38.dp, vertical = 16.dp)) {
                    Row {
                        ProvideTextStyle(
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        ) {
                            price()
                        }
                    }
                    ProvideTextStyle(
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    ) {
                        recurringText()
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Image(
                            painterResource(R.drawable.sub_plus),
                            colorFilter = ColorFilter.tint(colorResource(if (selected) R.color.yellow_100 else R.color.brand_400)),
                            contentDescription = null
                        )
                        ProvideTextStyle(
                            TextStyle(
                                fontSize = 15.sp
                            )
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
            Row(modifier = Modifier.padding(top = 16.dp).align(Alignment.TopEnd)) {
                Image(
                    painterResource(R.drawable.flag_flap),
                    contentDescription = null,
                )
                Text(
                    stringResource(R.string.current_plan),
                    color = colorResource(R.color.teal_1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Brush.horizontalGradient(fancyGradienColorList),)
                        .height(24.dp)
                        .padding(horizontal = 8.dp)
                        .wrapContentHeight()
                )
            }
        }
        AnimatedVisibility(selected,
            enter = slideInHorizontally(tween(durationMillis = 600, delayMillis = 50, easing = EaseInElastic)) {
                -it
            } + fadeIn(tween(durationMillis = 600, delayMillis = 50, easing = EaseInElastic)),
            exit = slideOutHorizontally { -it } + fadeOut()) {
            Image(painterResource(R.drawable.subscription_selected_indicator),
                contentDescription = null)
        }
    }
}

@HiltViewModel
class ChangeSubscriptionViewModel @Inject constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    private val purchaseHandler: PurchaseHandler
): BaseViewModel(userRepository, userViewModel) {
    fun selectPlan(plan: HabiticaProduct) {
        selectedPlan.value = plan
    }

    val products = listOf(
        HabiticaProduct.SUBSCRIPTION_1_MONTH,
        HabiticaProduct.SUBSCRIPTION_3_MONTH,
        HabiticaProduct.SUBSCRIPTION_6_MONTH,
        HabiticaProduct.SUBSCRIPTION_12_MONTH
    )

    val currentStep = MutableStateFlow(0)

    val activeSubscriptionPlan = MutableStateFlow<SubscriptionPlan?>(null)
    val currentPlan = MutableStateFlow<HabiticaProduct?>(null)
    val selectedPlan = MutableStateFlow(HabiticaProduct.SUBSCRIPTION_1_MONTH)

    val productDetails = MutableStateFlow<Map<HabiticaProduct, ProductDetails>>(emptyMap())

    init {
        val plan = userViewModel.user.value?.purchased?.plan
        currentPlan.value = when (plan?.planId) {
            "basic_earned" -> HabiticaProduct.SUBSCRIPTION_1_MONTH
            "basic_3mo" -> HabiticaProduct.SUBSCRIPTION_3_MONTH
            "basic_6mo" -> HabiticaProduct.SUBSCRIPTION_6_MONTH
            "basic_12mo" -> HabiticaProduct.SUBSCRIPTION_12_MONTH
            else -> null
        }
        if (currentPlan.value != null) {
            activeSubscriptionPlan.value = plan
            selectedPlan.value = currentPlan.value!!
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
    }

    fun productDetailsForProduct(product: HabiticaProduct): ProductDetails? {
        return productDetails.value[product]
    }

    fun purchaseSubscription(activity: Activity) {
        val details = productDetailsForProduct(selectedPlan.value) ?: return
        viewModelScope.launchCatching {
            purchaseHandler.purchase(activity, details)
        }
    }

    fun nextStep() {
        currentStep.value += 1
    }

    fun estimatedYearlyPrice(): String {
        val details = productDetailsForProduct(HabiticaProduct.SUBSCRIPTION_1_MONTH) ?: return ""
        val monthlyPricePhase = details.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.firstOrNull { it.priceAmountMicros > 0 } ?: return ""
        var yearlyPrice = (monthlyPricePhase.priceAmountMicros * 12).div(1_000_000.0)
        yearlyPrice = yearlyPrice.round(0) - 0.01
        val currency = Currency.getInstance(monthlyPricePhase.priceCurrencyCode)
        return if (monthlyPricePhase.formattedPrice.indexOf(currency.symbol) > 0) {
            "${"%,.2f".format(yearlyPrice)}${currency.symbol} "
        } else {
            "${currency.symbol}${"%,.2f".format(yearlyPrice)}"
        }
    }
}

@Composable
private fun ChangeSubscriptionChoiceView(modifier: Modifier = Modifier,
                                         viewModel: ChangeSubscriptionViewModel = viewModel()) {
    val currentPlan by viewModel.currentPlan.collectAsState(null)
    val selectedSub by viewModel.selectedPlan.collectAsState()
    val canContinue = selectedSub != currentPlan
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 16.dp)) {
        HabiticaButton(
            colorResource(R.color.yellow_100),
            colorResource(R.color.brand_100), {
                viewModel.nextStep()
            },
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.padding(top = 20.dp).alpha(if (canContinue) 1f else 0.5f),
            enabled = canContinue
        ) {
            Text(stringResource(R.string.action_continue))
        }
        Text(
            stringResource(R.string.review_subscription_change_info),
            fontStyle = FontStyle.Italic,
            color = colorResource(R.color.white),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 10.dp).fillMaxWidth()
        )
    }
}

@Composable
private fun ChangeSubscriptionReviewView(modifier: Modifier = Modifier,
                                         viewModel: ChangeSubscriptionViewModel = viewModel()) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 16.dp)) {
        Text("Placeholder Text\n".repeat(15))
        val activity = LocalActivity.current
        HabiticaButton(
            colorResource(R.color.yellow_100),
            colorResource(R.color.brand_100),
            {
                activity?.let { viewModel.purchaseSubscription(it) }
            },
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(stringResource(R.string.complete_purchase))
        }
    }
 }

@Composable
fun ChangeSubscriptionScreen(modifier: Modifier = Modifier, viewModel: ChangeSubscriptionViewModel = viewModel()) {
    val step by viewModel.currentStep.collectAsState()
    val activeSub by viewModel.activeSubscriptionPlan.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp).animateContentSize()) {
            Text(
                stringResource(R.string.change_subscription_plan),
                color = colorResource(R.color.white),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 20.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(colorResource(R.color.brand_400))) {}
                Image(painterResource(R.drawable.separator_fancy), contentDescription = null)
                Box(modifier = Modifier.weight(1f).height(1.dp).background(colorResource(R.color.brand_400))) {}
            }

            val currentPlan by viewModel.currentPlan.collectAsState(null)
            val selectedSub by viewModel.selectedPlan.collectAsState()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.animateContentSize()) {
                val details by viewModel.productDetails.collectAsState()
                for (product in viewModel.products) {
                    AnimatedVisibility(step == 0 || product == selectedSub) {
                        if (product == HabiticaProduct.SUBSCRIPTION_12_MONTH) {
                            ChangeSubscriptionOption(
                                price = { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(details[product]?.formattedSubscriptionPrice ?: "", style = TextStyle(
                                        brush = Brush.horizontalGradient(fancyGradienColorList),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    ))
                                    Text(viewModel.estimatedYearlyPrice(),
                                        textDecoration = TextDecoration.LineThrough,
                                        color = colorResource(if (selectedSub == product) R.color.gray_400 else R.color.brand_600),
                                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                } },
                                recurringText = { Text(stringResource(R.string.subscription_duration, stringResource(product.recurranceStringRes))) },
                                benefitLine =
                                    if (activeSub?.totalNumberOfGems != 50) {{
                                        Text(stringResource(R.string.raises_gem_cap_text))
                                    }} else null,
                                bottomView = if (activeSub?.isEligableForHourglassPromo == true) {{
                                    Text(stringResource(R.string.get_12_mystic_hourglasses),
                                        color = colorResource(R.color.teal_1),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.background(Brush.horizontalGradient(fancyGradienColorList),)
                                            .padding(horizontal = 24.dp, vertical = 12.dp))
                                }} else null,
                                selected = selectedSub == product,
                                isCurrentPlan = currentPlan == product,
                                selectedTextColor = colorResource(R.color.teal_1),
                                modifier = Modifier.clickable {
                                    viewModel.selectPlan(product)
                                }
                            )
                        } else {
                            ChangeSubscriptionOption(
                                price = { Text(details[product]?.formattedSubscriptionPrice ?: "") },
                                recurringText = { Text(stringResource(R.string.subscription_duration, stringResource(product.recurranceStringRes))) },
                                selected = selectedSub == product,
                                isCurrentPlan = currentPlan == product,
                                modifier = Modifier.clickable {
                                    viewModel.selectPlan(product)
                                }
                            )
                        }
                    }
                }
            }
            AnimatedContent(step) {
                when (it) {
                    0 -> ChangeSubscriptionChoiceView(viewModel = viewModel)
                    else -> ChangeSubscriptionReviewView(viewModel = viewModel)
                }
            }
        }
        Image(painterResource(R.drawable.footer_hills),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth())
        Text(stringResource(if (step == 0) R.string.subscriptions_renew_info else R.string.subscription_renew_review_info),
            color = colorResource(R.color.white),
            fontSize = 12.sp,
            modifier = Modifier.background(colorResource(R.color.brand_400))
                .animateContentSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom=20.dp))
    }
}

@AndroidEntryPoint
open class ChangeSubscriptionFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentComposeBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        _binding?.root?.setContent {
            HabiticaTheme {
                ChangeSubscriptionScreen()
            }
        }
        return binding.root
    }
}