package com.habitrpg.android.habitica.ui.viewmodels.inventory.equipment

import androidx.lifecycle.SavedStateHandle
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import javax.inject.Inject

class EquipmentOverviewViewModel(savedStateHandle: SavedStateHandle): BaseViewModel() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    fun getGear(key: String, onSuccess: (Equipment) -> Unit) {
        disposable.add(inventoryRepository.getEquipment(key).subscribe( {
            onSuccess(it)
        }, RxErrorHandler.handleEmptyError()))
    }
}
