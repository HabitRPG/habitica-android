package com.habitrpg.android.habitica.ui.viewmodels.inventory.equipment

import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EquipmentOverviewViewModel
@Inject
constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val inventoryRepository: InventoryRepository
) : BaseViewModel(userRepository, userViewModel) {
    val usesAutoEquip: Boolean
        get() = user.value?.preferences?.autoEquip == true
    val usesCostume: Boolean
        get() = user.value?.preferences?.costume == true

    fun getGear(
        key: String,
        onSuccess: (Equipment) -> Unit
    ) {
        viewModelScope.launchCatching {
            inventoryRepository.getEquipment(key).collect {
                onSuccess(it)
            }
        }
    }
}
