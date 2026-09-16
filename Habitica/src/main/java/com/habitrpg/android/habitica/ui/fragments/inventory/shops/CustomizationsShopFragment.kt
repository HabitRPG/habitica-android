package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.ui.viewmodels.inventory.shops.CustomizationShopViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomizationsShopFragment : ShopFragment<CustomizationShopViewModel>() {
    override val viewModel: CustomizationShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        shopIdentifier = Shop.CUSTOMIZATIONS
        super.onCreate(savedInstanceState)
    }
}
