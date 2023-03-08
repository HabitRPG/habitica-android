package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.models.shops.Shop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestShopFragment : ShopFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shopIdentifier = Shop.QUEST_SHOP
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
