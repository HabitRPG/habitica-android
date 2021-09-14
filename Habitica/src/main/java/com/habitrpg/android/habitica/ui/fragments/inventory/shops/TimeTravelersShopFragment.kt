package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.models.shops.Shop

class TimeTravelersShopFragment : ShopFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        shopIdentifier = Shop.TIME_TRAVELERS_SHOP
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currencyView.goldVisibility = View.GONE
        currencyView.gemVisibility = View.GONE
        currencyView.hourglassVisibility = View.VISIBLE
    }
}
