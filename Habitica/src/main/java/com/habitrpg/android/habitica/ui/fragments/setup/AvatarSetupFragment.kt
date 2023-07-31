package com.habitrpg.android.habitica.ui.fragments.setup

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentSetupAvatarBinding
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.SetupActivity
import com.habitrpg.android.habitica.ui.adapter.setup.CustomizationSetupAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.setup.AvatarCategoryView
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import java.util.Random
import javax.inject.Inject

@AndroidEntryPoint
class AvatarSetupFragment : BaseFragment<FragmentSetupAvatarBinding>() {

    @Inject
    lateinit var customizationRepository: SetupCustomizationRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override var binding: FragmentSetupAvatarBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSetupAvatarBinding {
        return FragmentSetupAvatarBinding.inflate(inflater, container, false)
    }

    var activity: SetupActivity? = null
    var width: Int = 0

    internal var adapter: CustomizationSetupAdapter? = null

    private var user: User? = null
    private var subcategories: List<String> = emptyList()
    private var activeButton: AvatarCategoryView? = null
    private var activeCategory: String? = null
    private var activeSubCategory: String? = null
    private var random = Random()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.adapter = CustomizationSetupAdapter()
        this.adapter?.userSize = this.user?.preferences?.size ?: "slim"
        adapter?.onUpdateUser = {
            lifecycleScope.launchCatching {
                userRepository.updateUser(it)
            }
        }
        adapter?.onEquipGear = {
            lifecycleScope.launchCatching {
                inventoryRepository.equip("equipped", it)
            }
        }

        this.adapter?.user = this.user
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding?.customizationDrawer?.binding?.customizationList?.layoutManager = layoutManager

        binding?.customizationDrawer?.binding?.customizationList?.adapter = this.adapter

        binding?.customizationDrawer?.binding?.subcategoryTabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if (position < subcategories.size) {
                    activeSubCategory = subcategories[position]
                }
                loadCustomizations()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) { /* no-on */ }

            override fun onTabReselected(tab: TabLayout.Tab) { /* no-on */ }
        })

        binding?.customizationDrawer?.binding?.bodyButton?.setOnClickListener { selectedBodyCategory() }
        binding?.customizationDrawer?.binding?.skinButton?.setOnClickListener { selectedSkinCategory() }
        binding?.customizationDrawer?.binding?.hairButton?.setOnClickListener { selectedHairCategory() }
        binding?.customizationDrawer?.binding?.extrasButton?.setOnClickListener { selectedExtrasCategory() }
        binding?.randomizeButton?.setOnClickListener { randomizeCharacter() }

        this.selectedBodyCategory()

        if (this.user != null) {
            this.updateAvatar()
        }
    }

    override fun onResume() {
        super.onResume()
        if (this.user != null) {
            this.updateAvatar()
        }
        this.selectedBodyCategory()
        if (context != null) {
            binding?.speechBubble?.animateText(context?.getString(R.string.avatar_setup_description) ?: "")
        }
    }

    private fun loadCustomizations() {
        val user = this.user ?: return
        val activeCategory = this.activeCategory ?: return

        this.adapter?.setCustomizationList(customizationRepository.getCustomizations(activeCategory, activeSubCategory, user))
    }

    fun setUser(user: User?) {
        this.user = user
        if (binding?.avatarView != null) {
            updateAvatar()
        }
        if (this.adapter != null) {
            this.adapter?.user = user
            this.adapter?.notifyDataSetChanged()
            loadCustomizations()
        }
    }

    private fun updateAvatar() {
        user?.let {
            binding?.avatarView?.setAvatar(it)
        }
    }

    private fun selectedBodyCategory() {
        activateButton(binding?.customizationDrawer?.binding?.bodyButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_BODY
        binding?.customizationDrawer?.binding?.subcategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_SIZE, SetupCustomizationRepository.SUBCATEGORY_SHIRT)
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_size)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_shirt)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun selectedSkinCategory() {
        activateButton(binding?.customizationDrawer?.binding?.skinButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_SKIN
        binding?.customizationDrawer?.binding?.subcategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_COLOR)
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_skin_color)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun selectedHairCategory() {
        activateButton(binding?.customizationDrawer?.binding?.hairButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_HAIR
        binding?.customizationDrawer?.binding?.subcategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_BANGS, SetupCustomizationRepository.SUBCATEGORY_COLOR, SetupCustomizationRepository.SUBCATEGORY_PONYTAIL)
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_hair_bangs)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_hair_color)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_hair_ponytail)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun selectedExtrasCategory() {
        activateButton(binding?.customizationDrawer?.binding?.extrasButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_EXTRAS
        binding?.customizationDrawer?.binding?.subcategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_GLASSES, SetupCustomizationRepository.SUBCATEGORY_FLOWER, SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR)
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_glasses)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_flower)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        binding?.customizationDrawer?.binding?.subcategoryTabs?.newTab()?.setText(R.string.avatar_wheelchair)?.let { binding?.customizationDrawer?.binding?.subcategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun randomizeCharacter() {
        val user = this.user ?: return
        val updateData = HashMap<String, Any>()
        updateData["preferences.size"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_BODY, SetupCustomizationRepository.SUBCATEGORY_SIZE, user), false)
        updateData["preferences.shirt"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_BODY, SetupCustomizationRepository.SUBCATEGORY_SHIRT, user), false)
        updateData["preferences.skin"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_SKIN, SetupCustomizationRepository.SUBCATEGORY_COLOR, user), false)
        updateData["preferences.hair.color"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_COLOR, user), false)
        updateData["preferences.hair.base"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_PONYTAIL, user), false)
        updateData["preferences.hair.bangs"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_HAIR, SetupCustomizationRepository.SUBCATEGORY_BANGS, user), false)
        updateData["preferences.hair.flower"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_FLOWER, user), true)
        updateData["preferences.chair"] = chooseRandomKey(customizationRepository.getCustomizations(SetupCustomizationRepository.CATEGORY_EXTRAS, SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR, user), true)
        lifecycleScope.launchCatching {
            userRepository.updateUser(updateData)
        }
    }

    @Suppress("ReturnCount")
    private fun chooseRandomKey(customizations: List<SetupCustomization>, weighFirstOption: Boolean): String {
        if (customizations.isEmpty()) {
            return ""
        }
        if (weighFirstOption) {
            if (random.nextInt(10) > 3) {
                return customizations[0].key
            }
        }
        return customizations[random.nextInt(customizations.size)].key
    }

    private fun activateButton(button: AvatarCategoryView?) {
        if (this.activeButton != null) {
            this.activeButton?.setActive(false)
        }
        this.activeButton = button
        this.activeButton?.setActive(true)
        val location = IntArray(2)
        val params = binding?.customizationDrawer?.binding?.caretView?.layoutParams as? RelativeLayout.LayoutParams
        this.activeButton?.getLocationOnScreen(location)
        val r = resources
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, r.displayMetrics).toInt()
        params?.marginStart = location[0] + px
        binding?.customizationDrawer?.binding?.caretView?.layoutParams = params
    }
}
