package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.ui.viewmodels.inventory.shops.MarketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarketFragment : ShopFragment<MarketViewModel>() {
    override val viewModel: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        shopIdentifier = Shop.MARKET
        super.onCreate(savedInstanceState)
    }
}
