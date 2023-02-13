package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle

class EquipmentOverviewFragment : AvatarOverviewFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        showCustomization = false
        super.onCreate(savedInstanceState)
    }
}
