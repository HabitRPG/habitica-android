package com.habitrpg.android.habitica.ui.fragments.setup

import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.SpeechBubbleView
import com.habitrpg.android.habitica.ui.activities.SetupActivity
import com.habitrpg.android.habitica.ui.adapter.setup.CustomizationSetupAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.setup.AvatarCategoryView
import org.greenrobot.eventbus.EventBus
import java.util.*
import javax.inject.Inject

class AvatarSetupFragment : BaseFragment() {

    @Inject
    lateinit var customizationRepository: SetupCustomizationRepository
    @Inject
    lateinit var apiClient: ApiClient
    
    var activity: SetupActivity? = null
    var width: Int = 0
    private val avatarView: AvatarView? by bindView(R.id.avatarView)
    private val customizationList: RecyclerView? by bindView(R.id.customization_list)
    private val subCategoryTabs: TabLayout? by bindView(R.id.subcategory_tabs)
    private val bodyButton: AvatarCategoryView? by bindView(R.id.body_button)
    private val skinButton: AvatarCategoryView? by bindView(R.id.skin_button)
    private val hairButton: AvatarCategoryView? by bindView(R.id.hair_button)
    private val extrasButton: AvatarCategoryView? by bindView(R.id.extras_button)
    private val caretView: ImageView? by bindView(R.id.caret_view)
    private val speechBubbleView: SpeechBubbleView? by bindView(R.id.speech_bubble)
    private val randomizeButton: Button? by bindView(R.id.randomize_button)

    internal var adapter: CustomizationSetupAdapter? = null
    internal var layoutManager: LinearLayoutManager = LinearLayoutManager(activity)

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

        this.adapter?.user = this.user
        this.layoutManager = LinearLayoutManager(activity)
        this.layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        this.customizationList?.layoutManager = this.layoutManager

        this.customizationList?.adapter = this.adapter

        this.subCategoryTabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if (position < subcategories.size) {
                    activeSubCategory = subcategories[position]
                }
                loadCustomizations()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && context != null) {
            speechBubbleView?.animateText(context?.getString(R.string.avatar_setup_description) ?: "")
        }
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun loadCustomizations() {
        if (this.user == null || this.adapter == null || this.activeCategory == null) {
            return
        }

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
        user.notNull {
            avatarView?.setAvatar(it)
        }
    }


    fun selectedBodyCategory() {
        activateButton(bodyButton)
        this.activeCategory = "body"
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = Arrays.asList("size", "shirt")
        subCategoryTabs?.newTab()?.setText(R.string.avatar_size).notNull { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_shirt).notNull { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    fun selectedSkinCategory() {
        activateButton(skinButton)
        this.activeCategory = "skin"
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = listOf("color")
        subCategoryTabs?.newTab()?.setText(R.string.avatar_skin_color).notNull { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    fun selectedHairCategory() {
        activateButton(hairButton)
        this.activeCategory = "hair"
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = Arrays.asList("bangs", "color", "ponytail")
        subCategoryTabs?.newTab()?.setText(R.string.avatar_hair_bangs).notNull { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_hair_color).notNull { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_hair_ponytail).notNull { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    fun selectedExtrasCategory() {
        activateButton(extrasButton)
        this.activeCategory = "extras"
        this.subCategoryTabs?.removeAllTabs()
        this.subcategories = Arrays.asList("glasses", "flower", "wheelchair")
        subCategoryTabs?.newTab()?.setText(R.string.avatar_glasses).notNull { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_flower).notNull { this.subCategoryTabs?.addTab(it) }
        subCategoryTabs?.newTab()?.setText(R.string.avatar_wheelchair).notNull { this.subCategoryTabs?.addTab(it) }
        loadCustomizations()
    }

    fun randomizeCharacter() {
        if (user == null) {
            return
        }
        val command = UpdateUserCommand()
        val updateData = HashMap<String, Any>()
        updateData["preferences.size"] = chooseRandomKey(customizationRepository.getCustomizations("body", "size", user), false)
        updateData["preferences.shirt"] = chooseRandomKey(customizationRepository.getCustomizations("body", "shirt", user), false)
        updateData["preferences.skin"] = chooseRandomKey(customizationRepository.getCustomizations("skin", "color", user), false)
        updateData["preferences.hair.color"] = chooseRandomKey(customizationRepository.getCustomizations("hair", "color", user), false)
        updateData["preferences.hair.base"] = chooseRandomKey(customizationRepository.getCustomizations("hair", "ponytail", user), false)
        updateData["preferences.hair.bangs"] = chooseRandomKey(customizationRepository.getCustomizations("hair", "bangs", user), false)
        updateData["preferences.hair.flower"] = chooseRandomKey(customizationRepository.getCustomizations("extras", "flower", user), true)
        updateData["preferences.chair"] = chooseRandomKey(customizationRepository.getCustomizations("extras", "wheelchair", user), true)
        command.updateData = updateData

        EventBus.getDefault().post(command)
    }

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
        val params = this.caretView?.layoutParams as RelativeLayout.LayoutParams
        this.activeButton?.getLocationOnScreen(location)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val r = resources
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, r.displayMetrics).toInt()
            params.marginStart = location[0] + px
            this.caretView?.layoutParams = params
        } else {
            caretView?.visibility = View.GONE
        }

    }
}
