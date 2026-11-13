package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.ui.viewmodels.inventory.shops.SeasonalShopViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeasonalShopFragment : ShopFragment<SeasonalShopViewModel>() {
    override val viewModel: SeasonalShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        shopIdentifier = Shop.SEASONAL_SHOP
        super.onCreate(savedInstanceState)
    }
}
