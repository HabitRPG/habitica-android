package com.habitrpg.android.habitica.ui.fragments.inventory.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_equipment_overview.*
import javax.inject.Inject

class EquipmentOverviewFragment : BaseMainFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private var nameMapping: MutableMap<String, String> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_equipment_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        battleGearHeadView.setOnClickListener { displayEquipmentDetailList("head", user?.items?.gear?.equipped?.head, false) }
        battleGearHeadAccessoryView.setOnClickListener { displayEquipmentDetailList("headAccessory", user?.items?.gear?.equipped?.headAccessory, false) }
        battleGearEyewearView.setOnClickListener { displayEquipmentDetailList("eyewear", user?.items?.gear?.equipped?.eyeWear, false) }
        battleGearArmorView.setOnClickListener { displayEquipmentDetailList("armor", user?.items?.gear?.equipped?.armor, false) }
        battleGearBackView.setOnClickListener { displayEquipmentDetailList("back", user?.items?.gear?.equipped?.back, false) }
        battleGearBodyView.setOnClickListener { displayEquipmentDetailList("body", user?.items?.gear?.equipped?.body, false) }
        battleGearWeaponView.setOnClickListener { displayEquipmentDetailList("weapon", user?.items?.gear?.equipped?.weapon, false) }
        battleGearShieldView.setOnClickListener { displayEquipmentDetailList("shield", user?.items?.gear?.equipped?.shield, false) }

        costumeHeadView.setOnClickListener { displayEquipmentDetailList("head", user?.items?.gear?.costume?.head, true) }
        costumeHeadAccessoryView.setOnClickListener { displayEquipmentDetailList("headAccessory", user?.items?.gear?.costume?.headAccessory, true) }
        costumeEyewearView.setOnClickListener { displayEquipmentDetailList("eyewear", user?.items?.gear?.costume?.eyeWear, true) }
        costumeArmorView.setOnClickListener { displayEquipmentDetailList("armor", user?.items?.gear?.costume?.armor, true) }
        costumeBackView.setOnClickListener { displayEquipmentDetailList("back", user?.items?.gear?.costume?.back, true) }
        costumeBodyView.setOnClickListener { displayEquipmentDetailList("body", user?.items?.gear?.costume?.body, true) }
        costumeWeaponView.setOnClickListener { displayEquipmentDetailList("weapon", user?.items?.gear?.costume?.weapon, true) }
        costumeShieldView.setOnClickListener { displayEquipmentDetailList("shield", user?.items?.gear?.costume?.shield, true) }

        costumeSwitch.isChecked = user?.preferences?.costume ?: false

        costumeSwitch.setOnCheckedChangeListener { _, isChecked -> userRepository.updateUser(user, "preferences.costume", isChecked).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }

        setImageNames()

        if (this.nameMapping.isEmpty()) {
            compositeSubscription.add(inventoryRepository.getOwnedEquipment().subscribe(Consumer {
                for (gear in it) {
                    this.nameMapping[gear.key ?: ""] = gear.text
                }

                setEquipmentNames()
            }, RxErrorHandler.handleEmptyError()))
        } else {
            setEquipmentNames()
        }
    }

    private fun setEquipmentNames() {
        battleGearHeadView.equipmentName = nameMapping[user?.items?.gear?.equipped?.head ?: ""]
        battleGearHeadAccessoryView.equipmentName = nameMapping[user?.items?.gear?.equipped?.headAccessory ?: ""]
        battleGearEyewearView.equipmentName = nameMapping[user?.items?.gear?.equipped?.eyeWear ?: ""]
        battleGearArmorView.equipmentName = nameMapping[user?.items?.gear?.equipped?.armor ?: ""]
        battleGearBackView.equipmentName = nameMapping[user?.items?.gear?.equipped?.back ?: ""]
        battleGearBodyView.equipmentName = nameMapping[user?.items?.gear?.equipped?.body ?: ""]
        battleGearWeaponView.equipmentName = nameMapping[user?.items?.gear?.equipped?.weapon ?: ""]
        battleGearShieldView.equipmentName = nameMapping[user?.items?.gear?.equipped?.shield ?: ""]

        costumeHeadView.equipmentName = nameMapping[user?.items?.gear?.costume?.head ?: ""]
        costumeHeadAccessoryView.equipmentName = nameMapping[user?.items?.gear?.costume?.headAccessory ?: ""]
        costumeEyewearView.equipmentName = nameMapping[user?.items?.gear?.costume?.eyeWear ?: ""]
        costumeArmorView.equipmentName = nameMapping[user?.items?.gear?.costume?.armor ?: ""]
        costumeBackView.equipmentName = nameMapping[user?.items?.gear?.costume?.back ?: ""]
        costumeBodyView.equipmentName = nameMapping[user?.items?.gear?.costume?.body ?: ""]
        costumeWeaponView.equipmentName = nameMapping[user?.items?.gear?.costume?.weapon ?: ""]
        costumeShieldView.equipmentName = nameMapping[user?.items?.gear?.costume?.shield ?: ""]
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun setImageNames() {
        battleGearHeadView.equipmentIdentifier = user?.items?.gear?.equipped?.head
        battleGearHeadAccessoryView.equipmentIdentifier = user?.items?.gear?.equipped?.headAccessory
        battleGearEyewearView.equipmentIdentifier = user?.items?.gear?.equipped?.eyeWear
        battleGearArmorView.equipmentIdentifier = user?.items?.gear?.equipped?.armor
        battleGearBackView.equipmentIdentifier = user?.items?.gear?.equipped?.back
        battleGearBodyView.equipmentIdentifier = user?.items?.gear?.equipped?.body
        battleGearWeaponView.equipmentIdentifier = user?.items?.gear?.equipped?.weapon
        battleGearShieldView.equipmentIdentifier = user?.items?.gear?.equipped?.shield

        costumeHeadView.equipmentIdentifier = user?.items?.gear?.costume?.head
        costumeHeadAccessoryView.equipmentIdentifier = user?.items?.gear?.costume?.headAccessory
        costumeEyewearView.equipmentIdentifier = user?.items?.gear?.costume?.eyeWear
        costumeArmorView.equipmentIdentifier = user?.items?.gear?.costume?.armor
        costumeBackView.equipmentIdentifier = user?.items?.gear?.costume?.back
        costumeBodyView.equipmentIdentifier = user?.items?.gear?.costume?.body
        costumeWeaponView.equipmentIdentifier = user?.items?.gear?.costume?.weapon
        costumeShieldView.equipmentIdentifier = user?.items?.gear?.costume?.shield
    }

    private fun displayEquipmentDetailList(type: String, equipped: String?, isCostume: Boolean?) {
        MainNavigationController.navigate(EquipmentOverviewFragmentDirections.openEquipmentDetail(type, isCostume ?: false, equipped ?: ""))
    }

}
