package com.habitrpg.android.habitica.ui.fragments.inventory.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentEquipmentOverviewBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.shared.habitica.models.user.Gear
import com.habitrpg.shared.habitica.models.user.User
import io.reactivex.functions.Consumer
import javax.inject.Inject

class EquipmentOverviewFragment : BaseMainFragment() {

    private lateinit var binding: FragmentEquipmentOverviewBinding
    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentEquipmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override var user: User?
        get() = super.user
        set(value) {
            super.user = value
            if (this::binding.isInitialized) {
                value?.items?.gear?.let {
                    updateGearData(it)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.battlegearView.onNavigate = { type, equipped ->
            displayEquipmentDetailList(type, equipped, false)
        }
        binding.costumeView.onNavigate = { type, equipped ->
            displayEquipmentDetailList(type, equipped, true)
        }

        binding.autoEquipSwitch.isChecked = user?.preferences?.autoEquip ?: false
        binding.costumeSwitch.isChecked = user?.preferences?.costume ?: false

        binding.autoEquipSwitch.setOnCheckedChangeListener { _, isChecked -> userRepository.updateUser(user, "preferences.autoEquip", isChecked).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }
        binding.costumeSwitch.setOnCheckedChangeListener { _, isChecked -> userRepository.updateUser(user, "preferences.costume", isChecked).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }

        user?.items?.gear?.let {
            updateGearData(it)
        }
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun displayEquipmentDetailList(type: String, equipped: String?, isCostume: Boolean?) {
        MainNavigationController.navigate(EquipmentOverviewFragmentDirections.openEquipmentDetail(type, isCostume ?: false, equipped ?: ""))
    }

    private fun updateGearData(gear: Gear) {
        val gearEquipped = gear.equipped
        if (gearEquipped != null && gearEquipped.weapon.isNotEmpty()) {
            compositeSubscription.add(inventoryRepository.getEquipment(gear.equipped?.weapon ?: "").firstElement()
                    .subscribe(Consumer {
                        binding.battlegearView.updateData(gearEquipped, it.twoHanded)
                    }, RxErrorHandler.handleEmptyError()))
        } else if (gearEquipped != null) {
            binding.battlegearView.updateData(gearEquipped)
        }

        val gearCostume = gear.costume
        if (gearCostume != null && gearCostume.weapon.isNotEmpty()) {
            compositeSubscription.add(inventoryRepository.getEquipment(gear.costume?.weapon ?: "").firstElement()
                    .subscribe(Consumer {
                        binding.costumeView.updateData(gearCostume, it.twoHanded)
                    }, RxErrorHandler.handleEmptyError()))
        } else if (gearCostume != null) {
            binding.costumeView.updateData(gearCostume)
        }
    }
}
