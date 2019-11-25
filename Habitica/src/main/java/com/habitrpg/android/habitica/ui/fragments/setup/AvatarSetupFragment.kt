package com.habitrpg.android.habitica.ui.fragments.setup

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.SpeechBubbleView
import com.habitrpg.android.habitica.ui.activities.SetupActivity
import com.habitrpg.android.habitica.ui.adapter.setup.CustomizationSetupAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.setup.AvatarCategoryView
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject

class AvatarSetupFragment : BaseFragment() {

    @Inject
    lateinit var customizationRepository: SetupCustomizationRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    
    var activity: SetupActivity? = null
    var width: Int = 0
    private val avatarView: AvatarView? by bindOptionalView(R.id.avatarView)
    private val customizationList: RecyclerView? by bindOptionalView(R.id.customization_list)
    private val subCategoryTabs: TabLayout? by bindOptionalView(R.id.subcategory_tabs)
    private val bodyButton: AvatarCategoryView? by bindOptionalView(R.id.body_button)
    private val skinButton: AvatarCategoryView? by bindOptionalView(R.id.skin_button)
    private val hairButton: AvatarCategoryView? by bindOptionalView(R.id.hair_button)
    private val extrasButton: AvatarCategoryView? by bindOptionalView(R.id.extras_button)
    private val caretView: ImageView? by bindOptionalView(R.id.caret_view)
    private val speechBubbleView: SpeechBubbleView? by bindOptionalView(R.id.speech_bubble)
    private val randomizeButton: Button? by bindOptionalView(R.id.randomize_button)

    internal var adapter: CustomizationSetupAdapter? = null

    private var user: User? = null
    private var subcategories: List<String> = emptyList()
    private var activeButton: AvatarCategoryView? = null
    private var activeCategory: String? = null
    private var activeSubCategory: String? = null
    private var random = Random()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_setup_avatar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        this.adapter = CustomizationSetupAdapter()
        this.adapter?.userSize = this.user?.preferences?.size ?: "slim"
        adapter?.updateUserEvents?.flatMap { userRepository.updateUser(user, it) }?.subscribeWithErrorHandler(Consumer {})?.let { compositeSubscription.add(it) }
        adapter?.equipGearEvents?.flatMap { inventoryRepository.equip(user, "equipped", it) }?.subscribeWithErrorHandler(Consumer {})?.let { compositeSubscription.add(it) }

        this.adapter?.user = this.user
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.customizationList?.layoutManager = layoutManager

        this.customizationList?.adapter = this.adapter

        this.subCategoryTabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

        bodyButton?.setOnClickListener { selectedBodyCategory() }
        skinButton?.setOnClickListener { selectedSkinCategory() }
        hairButton?.setOnClickListener { selectedHairCategory() }
        extrasButton?.setOnClickListener { selectedExtrasCategory() }
        randomizeButton?.setOnClickListener { randomizeCharacter() }

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
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && context != null) {
            speechBubbleView?.animateText(context?.getString(R.string.avatar_setup_description) ?: "")
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun loadCustomizations() {
        val user = this.user ?: return
        val activeCategory = this.activeCategory ?: return

        this.adapter?.setCustomizationList(customizationRepository.getCustomizations(activeCategory, activeSubCategory, user))
    }

    fun setUser(user: User?) {
        this.user = user
        if (avatarView != null) {
            updateAvatar()
        }
        if (this.adapter != null) {
            this.adapter?.user = user
            this.adapter?.notifyDataSetChanged()
        }
    }

    private fun updateAvatar() {
        user?.let {
            avatarView?.setAvatar(it)
        }
    }

    private fun selectedBodyCategory() {
        activateButton(bodyButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_BODY
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_SIZE, SetupCustomizationRepository.SUBCATEGORY_SHIRT)
        subCategoryTabs?.newTab()?.setText(R.string.avatar_size)?.let { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_shirt)?.let { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun selectedSkinCategory() {
        activateButton(skinButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_SKIN
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_COLOR)
        subCategoryTabs?.newTab()?.setText(R.string.avatar_skin_color)?.let { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun selectedHairCategory() {
        activateButton(hairButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_HAIR
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_BANGS, SetupCustomizationRepository.SUBCATEGORY_COLOR, SetupCustomizationRepository.SUBCATEGORY_PONYTAIL)
        subCategoryTabs?.newTab()?.setText(R.string.avatar_hair_bangs)?.let { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_hair_color)?.let { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_hair_ponytail)?.let { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    private fun selectedExtrasCategory() {
        activateButton(extrasButton)
        this.activeCategory = SetupCustomizationRepository.CATEGORY_EXTRAS
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = listOf(SetupCustomizationRepository.SUBCATEGORY_GLASSES, SetupCustomizationRepository.SUBCATEGORY_FLOWER, SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR)
        subCategoryTabs?.newTab()?.setText(R.string.avatar_glasses)?.let { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_flower)?.let { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_wheelchair)?.let { this.subCategoryTabs?.addTab(it) }
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
        compositeSubscription.add(userRepository.updateUser(user, updateData).subscribeWithErrorHandler(Consumer {}))
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
        val params = this.caretView?.layoutParams as? RelativeLayout.LayoutParams
        this.activeButton?.getLocationOnScreen(location)
        val r = resources
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, r.displayMetrics).toInt()
        params?.marginStart = location[0] + px
        this.caretView?.layoutParams = params
    }


}
