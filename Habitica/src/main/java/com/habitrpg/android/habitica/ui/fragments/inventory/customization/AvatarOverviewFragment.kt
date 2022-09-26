package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentAvatarOverviewBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import javax.inject.Inject

class AvatarOverviewFragment : BaseMainFragment<FragmentAvatarOverviewBinding>(), AdapterView.OnItemSelectedListener {

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentAvatarOverviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAvatarOverviewBinding {
        return FragmentAvatarOverviewBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.avatarSizeSpinner?.onItemSelectedListener = this

        binding?.avatarShirtView?.setOnClickListener { displayCustomizationFragment("shirt", null) }
        binding?.avatarSkinView?.setOnClickListener { displayCustomizationFragment("skin", null) }
        binding?.avatarChairView?.setOnClickListener { displayCustomizationFragment("chair", null) }
        binding?.avatarGlassesView?.setOnClickListener { displayEquipmentFragment("eyewear", "glasses") }
        binding?.avatarAnimalEarsView?.setOnClickListener { displayEquipmentFragment("headAccessory", "animal") }
        binding?.avatarAnimalTailView?.setOnClickListener { displayEquipmentFragment("back", "animal") }
        binding?.avatarHeadbandView?.setOnClickListener { displayEquipmentFragment("headAccessory", "headband") }
        binding?.avatarHairColorView?.setOnClickListener { displayCustomizationFragment("hair", "color") }
        binding?.avatarHairBangsView?.setOnClickListener { displayCustomizationFragment("hair", "bangs") }
        binding?.avatarHairBaseView?.setOnClickListener { displayCustomizationFragment("hair", "base") }
        binding?.avatarAccentView?.setOnClickListener { displayCustomizationFragment("hair", "flower") }
        binding?.avatarHairBeardView?.setOnClickListener { displayCustomizationFragment("hair", "beard") }
        binding?.avatarHairMustacheView?.setOnClickListener { displayCustomizationFragment("hair", "mustache") }
        binding?.avatarBackgroundView?.setOnClickListener { displayCustomizationFragment("background", null) }

        userViewModel.user.observe(viewLifecycleOwner) { updateUser(it) }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun displayCustomizationFragment(type: String, category: String?) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openAvatarDetail(type, category ?: ""))
    }

    private fun displayEquipmentFragment(type: String, category: String?) {
        MainNavigationController.navigate(AvatarOverviewFragmentDirections.openAvatarEquipment(type, category ?: ""))
    }

    fun updateUser(user: User?) {
        this.setSize(user?.preferences?.size)
        setCustomizations(user)
    }

    private fun setCustomizations(user: User?) {
        if (user == null) return
        binding?.avatarShirtView?.customizationIdentifier = user.preferences?.size + "_shirt_" + user.preferences?.shirt
        binding?.avatarSkinView?.customizationIdentifier = "skin_" + user.preferences?.skin
        val chair = user.preferences?.chair
        binding?.avatarChairView?.customizationIdentifier = if (chair?.startsWith("handleless") == true) "chair_$chair" else chair
        binding?.avatarGlassesView?.equipmentIdentifier = user.equipped?.eyeWear
        binding?.avatarAnimalEarsView?.equipmentIdentifier = user.equipped?.headAccessory
        binding?.avatarHeadbandView?.equipmentIdentifier = user.equipped?.headAccessory
        binding?.avatarAnimalTailView?.equipmentIdentifier = user.equipped?.back
        binding?.avatarHairColorView?.customizationIdentifier = if (user.preferences?.hair?.color != null && user.preferences?.hair?.color != "") "hair_bangs_1_" + user.preferences?.hair?.color else ""
        binding?.avatarHairBangsView?.customizationIdentifier = if (user.preferences?.hair?.bangs != null && user.preferences?.hair?.bangs != 0) "hair_bangs_" + user.preferences?.hair?.bangs + "_" + user.preferences?.hair?.color else ""
        binding?.avatarHairBaseView?.customizationIdentifier = if (user.preferences?.hair?.base != null && user.preferences?.hair?.base != 0) "hair_base_" + user.preferences?.hair?.base + "_" + user.preferences?.hair?.color else ""
        binding?.avatarAccentView?.customizationIdentifier = if (user.preferences?.hair?.flower != null && user.preferences?.hair?.flower != 0) "hair_flower_" + user.preferences?.hair?.flower else ""
        binding?.avatarHairBeardView?.customizationIdentifier = if (user.preferences?.hair?.beard != null && user.preferences?.hair?.beard != 0) "hair_beard_" + user.preferences?.hair?.beard + "_" + user.preferences?.hair?.color else ""
        binding?.avatarHairMustacheView?.customizationIdentifier = if (user.preferences?.hair?.mustache != null && user.preferences?.hair?.mustache != 0) "hair_mustache_" + user.preferences?.hair?.mustache + "_" + user.preferences?.hair?.color else ""
        binding?.avatarBackgroundView?.customizationIdentifier = "background_" + user.preferences?.background
    }

    private fun setSize(size: String?) {
        if (size == null) {
            return
        }
        if (size == "slim") {
            binding?.avatarSizeSpinner?.setSelection(0, false)
        } else {
            binding?.avatarSizeSpinner?.setSelection(1, false)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val newSize: String = if (position == 0) "slim" else "broad"

        compositeSubscription.add(
            userRepository.updateUser("preferences.size", newSize)
                .subscribe({ }, ExceptionHandler.rx())
        )
    }

    override fun onNothingSelected(parent: AdapterView<*>) { /* no-on */ }
}
