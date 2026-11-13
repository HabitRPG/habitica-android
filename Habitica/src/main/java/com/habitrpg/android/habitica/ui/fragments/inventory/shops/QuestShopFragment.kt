package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.ui.viewmodels.inventory.shops.QuestShopViewModel
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestShopFragment : ShopFragment<QuestShopViewModel>() {
    override val viewModel: QuestShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        shopIdentifier = Shop.QUEST_SHOP
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchCatching {
            userRepository.getQuestAchievements().collect {
                adapter?.completedQuests = it.map { it.questKey }
            }
        }
    }
}
