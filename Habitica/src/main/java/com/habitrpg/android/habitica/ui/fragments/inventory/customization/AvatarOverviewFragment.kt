package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentAvatarOverviewBinding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import io.reactivex.functions.Consumer

class AvatarOverviewFragment : BaseMainFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentAvatarOverviewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAvatarOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.avatarSizeSpinner.onItemSelectedListener = this

        binding.avatarShirtView.setOnClickListener { displayCustomizationFragment("shirt", null) }
        binding.avatarSkinView.setOnClickListener { displayCustomizationFragment("skin", null) }
        binding.avatarChairView.setOnClickListener { displayCustomizationFragment("chair", null) }
        binding.avatarGlassesView.setOnClickListener { displayCustomizationFragment("eyewear", null) }
        binding.avatarAnimalEarsView.setOnClickListener { displayCustomizationFragment("animal_ears", null) }
        binding.avatarAnimalTailView.setOnClickListener { displayCustomizationFragment("animal_tails", null) }
        binding.avatarHairColorView.setOnClickListener { displayCustomizationFragment("hair", "color") }
        binding.avatarHairBangsView.setOnClickListener { displayCustomizationFragment("hair", "bangs") }
        binding.avatarHairBaseView.setOnClickListener { displayCustomizationFragment("hair", "base") }
        binding.avatarHairFlowerView.setOnClickListener { displayCustomizationFragment("hair", "flower") }
        binding.avatarHairBeardView.setOnClickListener { displayCustomizationFragment("hair", "beard") }
        binding.avatarHairMustacheView.setOnClickListener { displayCustomizationFragment("hair", "mustache") }
        binding.avatarBackgroundView.setOnClickListener { displayCustomizationFragment("background", null) }

        compositeSubscription.add(userRepository.getUser().subscribeWithErrorHandler(Consumer {
            updateUser(it)
        }))
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun displayCustomizationFragment(type: String, category: String?) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openAvatarDetail(type, category ?: ""))
    }

    fun updateUser(user: User) {
        this.setSize(user.preferences?.size)
        setCustomizations(user)
    }

    private fun setCustomizations(user: User) {
        binding.avatarShirtView.customizationIdentifier = user.preferences?.size + "_shirt_" + user.preferences?.shirt
        binding.avatarShirtView.equipmentName = user.preferences?.shirt
        binding.avatarSkinView.customizationIdentifier = "skin_" + user.preferences?.skin
        binding.avatarSkinView.equipmentName = user.preferences?.skin
        val chair = user.preferences?.chair
        binding.avatarChairView.customizationIdentifier = if (chair?.startsWith("handleless") == true) "chair_$chair" else chair
        binding.avatarChairView.equipmentName = chair?.removePrefix("chair_")
        binding.avatarGlassesView.customizationIdentifier = "shop_" + user.equipped?.eyeWear
        binding.avatarGlassesView.equipmentName = user.equipped?.eyeWear
        binding.avatarAnimalEarsView.customizationIdentifier = "shop_" + user.equipped?.headAccessory
        binding.avatarAnimalEarsView.equipmentName = user.equipped?.headAccessory
        binding.avatarAnimalTailView.customizationIdentifier = "shop_" + user.equipped?.back
        binding.avatarAnimalTailView.equipmentName = user.equipped?.back
        binding.avatarHairColorView.customizationIdentifier = if (user.preferences?.hair?.color != null && user.preferences?.hair?.color != "") "hair_bangs_1_" + user.preferences?.hair?.color else ""
        binding.avatarHairColorView.equipmentName = user.preferences?.hair?.color
        binding.avatarHairBangsView.customizationIdentifier = if (user.preferences?.hair?.bangs != null && user.preferences?.hair?.bangs != 0) "hair_bangs_" + user.preferences?.hair?.bangs + "_" + user.preferences?.hair?.color else ""
        binding.avatarHairBangsView.equipmentName = user.preferences?.hair?.bangs.toString()
        binding.avatarHairBaseView.customizationIdentifier = if (user.preferences?.hair?.base != null && user.preferences?.hair?.base != 0) "hair_base_" + user.preferences?.hair?.base + "_" + user.preferences?.hair?.color else ""
        binding.avatarHairBaseView.equipmentName = user.preferences?.hair?.base.toString()
        binding.avatarHairFlowerView.customizationIdentifier = if (user.preferences?.hair?.flower != null && user.preferences?.hair?.flower != 0) "hair_flower_" + user.preferences?.hair?.flower else ""
        binding.avatarHairFlowerView.equipmentName = user.preferences?.hair?.bangs.toString()
        binding.avatarHairBeardView.customizationIdentifier = if (user.preferences?.hair?.beard != null && user.preferences?.hair?.beard != 0) "hair_beard_" + user.preferences?.hair?.beard + "_" + user.preferences?.hair?.color else ""
        binding.avatarHairBeardView.equipmentName = user.preferences?.hair?.beard.toString()
        binding.avatarHairMustacheView.customizationIdentifier = if (user.preferences?.hair?.mustache != null && user.preferences?.hair?.mustache != 0) "hair_mustache_" + user.preferences?.hair?.mustache + "_" + user.preferences?.hair?.color else ""
        binding.avatarHairMustacheView.equipmentName = user.preferences?.hair?.mustache.toString()
        binding.avatarBackgroundView.customizationIdentifier = "background_" + user.preferences?.background
        binding.avatarBackgroundView.equipmentName = user.preferences?.background
    }

    private fun setSize(size: String?) {
        if (size == null) {
            return
        }
        if (size == "slim") {
            binding.avatarSizeSpinner.setSelection(0, false)
        } else {
            binding.avatarSizeSpinner.setSelection(1, false)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val newSize: String = if (position == 0) "slim" else "broad"

        if (this.user?.isValid == true && this.user?.preferences?.size != newSize) {
            compositeSubscription.add(userRepository.updateUser(user, "preferences.size", newSize)
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) { /* no-on */ }


}
