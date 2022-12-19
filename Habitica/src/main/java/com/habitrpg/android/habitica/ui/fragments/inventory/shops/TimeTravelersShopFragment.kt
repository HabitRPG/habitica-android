package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.ui.views.CurrencyText

class TimeTravelersShopFragment : ShopFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shopIdentifier = Shop.TIME_TRAVELERS_SHOP
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeCurrencyViews()
    }

    override fun initializeCurrencyViews() {
        currencyView.setContent {
            hourglasses.value?.let { CurrencyText(currency = "hourglasses", value = it) }
        }
    }
}
