package com.habitrpg.android.habitica.ui.fragments.inventory.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentEquipmentOverviewBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.user.Gear
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.inventory.equipment.EquipmentOverviewViewModel
import com.habitrpg.android.habitica.ui.views.equipment.EquipmentOverviewView

class EquipmentOverviewFragment : BaseMainFragment<FragmentEquipmentOverviewBinding>() {

    private val viewModel: EquipmentOverviewViewModel by viewModels()

    override var binding: FragmentEquipmentOverviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentEquipmentOverviewBinding {
        return FragmentEquipmentOverviewBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.battlegearView?.onNavigate = { type, equipped ->
            displayEquipmentDetailList(type, equipped, false)
        }
        binding?.costumeView?.onNavigate = { type, equipped ->
            displayEquipmentDetailList(type, equipped, true)
        }

        binding?.autoEquipSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == viewModel.user.value?.preferences?.autoEquip) return@setOnCheckedChangeListener
            viewModel.updateUser("preferences.autoEquip", isChecked)
        }
        binding?.costumeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == viewModel.user.value?.preferences?.costume) return@setOnCheckedChangeListener
            viewModel.updateUser("preferences.costume", isChecked)
        }

        viewModel.user.observe(viewLifecycleOwner) {
            it?.items?.gear?.let {
                updateGearData(it)
            }
            binding?.autoEquipSwitch?.isChecked = viewModel.usesAutoEquip
            binding?.costumeSwitch?.isChecked = viewModel.usesCostume

            binding?.costumeView?.isEnabled = viewModel.usesCostume
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun displayEquipmentDetailList(type: String, equipped: String?, isCostume: Boolean?) {
        MainNavigationController.navigate(EquipmentOverviewFragmentDirections.openEquipmentDetail(type, isCostume ?: false, equipped ?: ""))
    }

    private fun updateGearData(gear: Gear) {
        updateOutfit(binding?.battlegearView, gear.equipped)
        updateOutfit(binding?.costumeView, gear.costume)
    }

    private fun updateOutfit(view: EquipmentOverviewView?, outfit: Outfit?) {
        if (outfit?.weapon?.isNotEmpty() == true) {
            viewModel.getGear(outfit.weapon) {
                view?.updateData(outfit, it.twoHanded)
            }
        } else {
            view?.updateData(outfit)
        }
    }
}
