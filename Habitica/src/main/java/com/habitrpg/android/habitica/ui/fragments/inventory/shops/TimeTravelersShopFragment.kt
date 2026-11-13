package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.ui.fragments.purchases.EventOutcomeSubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.viewmodels.inventory.shops.TimeTravelersShopViewModel
import com.habitrpg.android.habitica.ui.views.CurrencyText
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class TimeTravelersShopFragment : ShopFragment<TimeTravelersShopViewModel>() {
    override val viewModel: TimeTravelersShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        shopIdentifier = Shop.TIME_TRAVELERS_SHOP
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeCurrencyViews()

        viewLifecycleOwner.lifecycleScope.launchCatching {
            val user = viewModel.userViewModel.user.value
            if (user?.isSubscribed != true &&
                user
                    ?.purchased
                    ?.plan
                    ?.consecutive
                    ?.trinkets == 0
            ) {
                delay(2.seconds)
                val subscriptionBottomSheet =
                    EventOutcomeSubscriptionBottomSheetFragment().apply {
                        eventType =
                            EventOutcomeSubscriptionBottomSheetFragment.EVENT_HOURGLASS_SHOP_OPENED
                    }
                if (isAdded) {
                    activity?.supportFragmentManager?.let {
                        subscriptionBottomSheet.show(
                            it,
                            SubscriptionBottomSheetFragment.TAG,
                        )
                    }
                }
            }
        }
    }

    override fun initializeCurrencyViews() {
        currencyView.setContent {
            hourglasses.value?.let { CurrencyText(currency = "hourglasses", value = it) }
        }
    }
}
