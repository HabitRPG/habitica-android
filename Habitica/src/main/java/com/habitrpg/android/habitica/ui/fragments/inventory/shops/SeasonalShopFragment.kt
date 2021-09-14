package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.models.shops.Shop

class SeasonalShopFragment : ShopFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        shopIdentifier = Shop.SEASONAL_SHOP
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
