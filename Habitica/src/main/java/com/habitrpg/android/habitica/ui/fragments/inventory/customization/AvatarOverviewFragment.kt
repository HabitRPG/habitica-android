package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_avatar_overview.*

class AvatarOverviewFragment : BaseMainFragment(), AdapterView.OnItemSelectedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_avatar_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        avatarSizeSpinner.onItemSelectedListener = this

        avatarShirtView.setOnClickListener { displayCustomizationFragment("shirt", null) }
        avatarSkinView.setOnClickListener { displayCustomizationFragment("skin", null) }
        avatarChairView.setOnClickListener { displayCustomizationFragment("chair", null) }
        avatarHairColorView.setOnClickListener { displayCustomizationFragment("hair", "color") }
        avatarHairBangsView.setOnClickListener { displayCustomizationFragment("hair", "bangs") }
        avatarHairBaseView.setOnClickListener { displayCustomizationFragment("hair", "base") }
        avatarHairFlowerView.setOnClickListener { displayCustomizationFragment("hair", "flower") }
        avatarHairBeardView.setOnClickListener { displayCustomizationFragment("hair", "beard") }
        avatarHairMustacheView.setOnClickListener { displayCustomizationFragment("hair", "mustache") }
        avatarBackgroundView.setOnClickListener { displayCustomizationFragment("background", null) }

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
        avatarShirtView.customizationIdentifier = user.preferences?.size + "_shirt_" + user.preferences?.shirt
        avatarShirtView.equipmentName = user.preferences?.shirt
        avatarSkinView.customizationIdentifier = "skin_" + user.preferences?.skin
        avatarSkinView.equipmentName = user.preferences?.skin
        val chair = user.preferences?.chair
        avatarChairView.customizationIdentifier = if (chair?.startsWith("handleless") == true) "chair_$chair" else chair
        avatarChairView.equipmentName = chair?.removePrefix("chair_")
        avatarHairColorView.customizationIdentifier = if (user.preferences?.hair?.color != null && user.preferences?.hair?.color != "") "hair_bangs_1_" + user.preferences?.hair?.color else ""
        avatarHairColorView.equipmentName = user.preferences?.hair?.color
        avatarHairBangsView.customizationIdentifier = if (user.preferences?.hair?.bangs != null && user.preferences?.hair?.bangs != 0) "hair_bangs_" + user.preferences?.hair?.bangs + "_" + user.preferences?.hair?.color else ""
        avatarHairBangsView.equipmentName = user.preferences?.hair?.bangs.toString()
        avatarHairBaseView.customizationIdentifier = if (user.preferences?.hair?.base != null && user.preferences?.hair?.base != 0) "hair_base_" + user.preferences?.hair?.base + "_" + user.preferences?.hair?.color else ""
        avatarHairBaseView.equipmentName = user.preferences?.hair?.base.toString()
        avatarHairFlowerView.customizationIdentifier = if (user.preferences?.hair?.flower != null && user.preferences?.hair?.flower != 0) "hair_flower_" + user.preferences?.hair?.flower else ""
        avatarHairFlowerView.equipmentName = user.preferences?.hair?.bangs.toString()
        avatarHairBeardView.customizationIdentifier = if (user.preferences?.hair?.beard != null && user.preferences?.hair?.beard != 0) "hair_beard_" + user.preferences?.hair?.beard + "_" + user.preferences?.hair?.color else ""
        avatarHairBeardView.equipmentName = user.preferences?.hair?.beard.toString()
        avatarHairMustacheView.customizationIdentifier = if (user.preferences?.hair?.mustache != null && user.preferences?.hair?.mustache != 0) "hair_mustache_" + user.preferences?.hair?.mustache + "_" + user.preferences?.hair?.color else ""
        avatarHairMustacheView.equipmentName = user.preferences?.hair?.mustache.toString()
        avatarBackgroundView.customizationIdentifier = "background_" + user.preferences?.background
        avatarBackgroundView.equipmentName = user.preferences?.background
    }

    private fun setSize(size: String?) {
        if (avatarSizeSpinner == null || size == null) {
            return
        }
        if (size == "slim") {
            avatarSizeSpinner.setSelection(0, false)
        } else {
            avatarSizeSpinner.setSelection(1, false)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val newSize: String = if (position == 0) "slim" else "broad"

        if (this.user != null && this.user?.preferences?.size != newSize) {
            compositeSubscription.add(userRepository.updateUser(user, "preferences.size", newSize)
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}


}
