package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.ui.adapter.CustomizationRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.shared.habitica.models.inventory.Customization
import com.habitrpg.shared.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_recyclerview.*
import javax.inject.Inject

class AvatarCustomizationFragment : BaseMainFragment() {

    @Inject
    lateinit var customizationRepository: CustomizationRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository

    var type: String? = null
    var category: String? = null
    private var activeCustomization: String? = null

    internal var adapter: CustomizationRecyclerViewAdapter = CustomizationRecyclerViewAdapter()
    internal var layoutManager: GridLayoutManager = GridLayoutManager(activity, 2)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)

        compositeSubscription.add(adapter.getSelectCustomizationEvents()
                .flatMap { customization ->
                    userRepository.useCustomization(user, customization.type ?: "", customization.category, customization.identifier ?: "")
                }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(adapter.getUnlockCustomizationEvents()
                .flatMap { customization ->
                    val user = this.user
                    if (user != null) {
                    userRepository.unlockPath(user, customization)
                    } else {
                        Flowable.empty()
                    }
                }
                .flatMap { userRepository.retrieveUser(withTasks = false, forced = true) }
                .flatMap { inventoryRepository.retrieveInAppRewards() }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(adapter.getUnlockSetEvents()
                .flatMap { set ->
                    val user = this.user
                    if (user != null) {
                        userRepository.unlockPath(user, set)
                    } else {
                        Flowable.empty()
                    }
                 }
                .flatMap { userRepository.retrieveUser(withTasks = false, forced = true) }
                .flatMap { inventoryRepository.retrieveInAppRewards() }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = AvatarCustomizationFragmentArgs.fromBundle(it)
            type = args.type
            if (args.category.isNotEmpty()) {
                category = args.category
            }
        }

        val layoutManager = GridLayoutManager(activity, 4)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == 0) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
        setGridSpanCount(view.width)
        recyclerView.layoutManager = layoutManager

        recyclerView.addItemDecoration(MarginDecoration(context))

        recyclerView.adapter = adapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
        this.loadCustomizations()

        compositeSubscription.add(userRepository.getUser().subscribeWithErrorHandler(Consumer {
            updateUser(it)
        }))
    }

    override fun onDestroy() {
        customizationRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun loadCustomizations() {
        val type = this.type ?: return
        compositeSubscription.add(customizationRepository.getCustomizations(type, category, false).subscribe(Consumer<RealmResults<Customization>> { adapter.setCustomizations(it) }, RxErrorHandler.handleEmptyError()))
        if (type == "hair" && (category == "beard" || category == "mustache")) {
            val otherCategory = if (category == "mustache") "beard" else "mustache"
            compositeSubscription.add(customizationRepository.getCustomizations(type, otherCategory, true).subscribe(Consumer<RealmResults<Customization>> { adapter.additionalSetItems = it }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun setGridSpanCount(width: Int) {
        val itemWidth = context?.resources?.getDimension(R.dimen.customization_width) ?: 0F
        var spanCount = (width / itemWidth).toInt()
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager.spanCount = spanCount
    }

    fun updateUser(user: User) {
        this.updateActiveCustomization(user)
        if (adapter.customizationList.size != 0) {
            val ownedCustomizations = ArrayList<String>()
            user.purchased?.customizations?.filter { it.type == this.type && it.purchased }?.mapTo(ownedCustomizations) { it.key ?: "" }
            adapter.updateOwnership(ownedCustomizations)
        } else {
            this.loadCustomizations()
        }
        this.adapter.userSize = this.user?.preferences?.size ?: ""
        this.adapter.hairColor = this.user?.preferences?.hair?.color ?: ""
        this.adapter.gemBalance = user.gemCount ?: 0
        adapter.notifyDataSetChanged()
    }

    private fun updateActiveCustomization(user: User) {
        if (this.type == null || user.preferences == null) {
            return
        }
        val prefs = this.user?.preferences
        val activeCustomization = when (this.type) {
            "skin" -> prefs?.skin
            "shirt" -> prefs?.shirt
            "background" -> prefs?.background
            "hair" -> when (this.category) {
                "bangs" -> prefs?.hair?.bangs.toString()
                "base" -> prefs?.hair?.base.toString()
                "color" -> prefs?.hair?.color
                "flower" -> prefs?.hair?.flower.toString()
                "beard" -> prefs?.hair?.beard.toString()
                "mustache" -> prefs?.hair?.mustache.toString()
                 else -> ""
            }
            else -> ""
        }
        if (activeCustomization != null) {
            this.activeCustomization = activeCustomization
            this.adapter.activeCustomization = activeCustomization
        }
    }
}

