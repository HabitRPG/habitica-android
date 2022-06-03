package com.habitrpg.android.habitica.ui.views.equipment

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.EquipmentOverviewViewBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.models.user.Outfit

class EquipmentOverviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onNavigate: ((String, String) -> Unit)? = null
    private var binding: EquipmentOverviewViewBinding = EquipmentOverviewViewBinding.inflate(context.layoutInflater, this)

    init {
        background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_gray_50)
        setScaledPadding(context, 12, 12, 12, 12)
        orientation = VERTICAL

        binding.weaponItem.setOnClickListener { onNavigate?.invoke("weapon", binding.weaponItem.identifier) }
        binding.shieldItem.setOnClickListener { onNavigate?.invoke("shield", binding.shieldItem.identifier) }
        binding.headItem.setOnClickListener { onNavigate?.invoke("head", binding.headItem.identifier) }
        binding.armorItem.setOnClickListener { onNavigate?.invoke("armor", binding.armorItem.identifier) }
        binding.headAccessoryItem.setOnClickListener { onNavigate?.invoke("headAccessory", binding.headAccessoryItem.identifier) }
        binding.bodyItem.setOnClickListener { onNavigate?.invoke("body", binding.bodyItem.identifier) }
        binding.backItem.setOnClickListener { onNavigate?.invoke("back", binding.backItem.identifier) }
        binding.eyewearItem.setOnClickListener { onNavigate?.invoke("eyewear", binding.eyewearItem.identifier) }
    }

    fun updateData(outfit: Outfit?, isWeaponTwoHanded: Boolean = false) {
        binding.weaponItem.set(outfit?.weapon, isWeaponTwoHanded)
        binding.shieldItem.set(outfit?.shield, false, isWeaponTwoHanded)
        binding.headItem.set(outfit?.head)
        binding.armorItem.set(outfit?.armor)
        binding.headAccessoryItem.set(outfit?.headAccessory)
        binding.bodyItem.set(outfit?.body)
        binding.backItem.set(outfit?.back)
        binding.eyewearItem.set(outfit?.eyeWear)
    }
}
