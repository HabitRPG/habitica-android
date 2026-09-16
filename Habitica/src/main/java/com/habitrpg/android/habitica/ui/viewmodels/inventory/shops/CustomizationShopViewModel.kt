package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomizationShopViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    configManager: AppConfigManager,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel
) : ShopViewModel(inventoryRepository, configManager, userRepository, userViewModel) {
}

