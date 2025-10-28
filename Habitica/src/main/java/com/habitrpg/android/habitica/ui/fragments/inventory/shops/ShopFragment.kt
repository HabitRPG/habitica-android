package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.EventOutcomeSubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.CurrencyText
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.RecyclerViewState
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

open class ShopFragment : BaseMainFragment<FragmentRefreshRecyclerviewBinding>() {
    internal val currencyView: ComposeView by lazy {
        return@lazy ComposeView(requireContext())
    }

    var adapter: ShopRecyclerAdapter? = null
    var shopIdentifier: String? = null
    var shop: Shop? = null
    internal val hourglasses = mutableStateOf<Double?>(null)
    private val gems = mutableStateOf<Double?>(null)
    private val gold = mutableStateOf<Double?>(null)

    @Inject
    lateinit var contentRepository: ContentRepository

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    private var layoutManager: GridLayoutManager? = null

    private var gearCategories: MutableList<ShopCategory>? = null

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        inventoryRepository.close()
        socialRepository.close()
        toolbarAccessoryContainer?.removeView(currencyView)
        super.onDestroyView()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeCurrencyViews()
        toolbarAccessoryContainer?.addView(currencyView)
        binding?.recyclerView?.setBackgroundResource(R.color.content_background)
        binding?.recyclerView?.onRefresh = {
            loadShopInventory()
        }
        binding?.refreshLayout?.setOnRefreshListener {
            loadShopInventory()
        }
        adapter = binding?.recyclerView?.adapter as? ShopRecyclerAdapter
        if (adapter == null) {
            adapter = ShopRecyclerAdapter()
            adapter?.onNeedsRefresh = {
                loadShopInventory()
                if (Shop.MARKET == shopIdentifier) {
                    loadMarketGear()
                }
            }
            adapter?.onShowPurchaseDialog = { item, isPinned ->
                if (item.key == "gem" && userViewModel.user.value?.isSubscribed != true) {
                    Analytics.sendEvent("View gems for gold CTA", EventCategory.BEHAVIOUR, HitType.EVENT)
                    val subscriptionBottomSheet =
                        EventOutcomeSubscriptionBottomSheetFragment().apply {
                            eventType = EventOutcomeSubscriptionBottomSheetFragment.EVENT_GEMS_FOR_GOLD
                        }
                    activity?.let { activity ->
                        subscriptionBottomSheet.show(activity.supportFragmentManager, SubscriptionBottomSheetFragment.TAG)
                    }
                } else {
                    val dialog =
                        PurchaseDialog(
                            requireContext(),
                            item,
                            mainActivity
                        )
                    dialog.shopIdentifier = shopIdentifier
                    dialog.isPinned = isPinned
                    dialog.onShopNeedsRefresh = {
                        loadShopInventory()
                        if (Shop.MARKET == shopIdentifier) {
                            loadMarketGear()
                        }
                    }
                    dialog.show()
                }
            }

            adapter?.context = context
            adapter?.mainActivity = mainActivity
            binding?.recyclerView?.adapter = adapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
            adapter?.changeClassEvents = {
                showClassChangeDialog(it)
            }
            adapter?.emptySectionClickedEvents = {
                if (shopIdentifier == Shop.CUSTOMIZATIONS) {
                    var navigationID = R.id.ComposeAvatarCustomizationFragment
                    var type = ""
                    var category = ""
                    if (it == "color") {
                        type = "hair"
                        category = "color"
                    } else if (it == "facialHair") {
                        type = "hair"
                        category = "beard"
                    } else if (it == "base") {
                        type = "hair"
                        category = "base"
                    } else if (it == "animalEars") {
                        navigationID = R.id.composeAvatarEquipmentFragment
                        type = "headAccessory"
                        category = "animal"
                    } else if (it == "animalTails") {
                        navigationID = R.id.composeAvatarEquipmentFragment
                        type = "back"
                        category = "animal"
                    } else if (it == "backgrounds") {
                        type = "background"
                    } else {
                        type = it
                    }
                    MainNavigationController.navigate(navigationID, bundleOf("category" to category, "type" to type))
                } else if (shopIdentifier == Shop.TIME_TRAVELERS_SHOP) {
                    MainNavigationController.navigate(R.id.equipmentOverviewFragment)
                }
            }

            lifecycleScope.launchCatching {
                inventoryRepository.getInAppReward("armoire").collect {
                    adapter?.armoireItem = it
                }
            }
            lifecycleScope.launchCatching {
                inventoryRepository.getArmoireRemainingCount().collect {
                    adapter?.armoireCount = it
                }
            }
        }

        if (binding?.recyclerView?.layoutManager == null) {
            layoutManager = GridLayoutManager(context, 2)
            layoutManager?.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if ((adapter?.getItemViewType(position) ?: 0) < 5) {
                            layoutManager?.spanCount ?: 1
                        } else {
                            1
                        }
                    }
                }
            binding?.recyclerView?.layoutManager = layoutManager
        }

        if (savedInstanceState != null) {
            this.shopIdentifier = savedInstanceState.getString(SHOP_IDENTIFIER_KEY, "")
        }

        adapter?.selectedGearCategory = userViewModel.user.value?.stats?.habitClass ?: ""

        if (shop != null) {
            adapter?.setShop(shop)
        }
        adapter?.shopSpriteSuffix = configManager.shopSpriteSuffix()

        val categories = gearCategories
        if (categories != null) {
            adapter?.gearCategories = categories
        } else {
            if (Shop.MARKET == shopIdentifier) {
                loadMarketGear()
            }
        }

        userViewModel.user.observe(viewLifecycleOwner) {
            adapter?.user = it
            hourglasses.value = it?.hourglassCount?.toDouble() ?: 0.0
            gems.value = it?.gemCount?.toDouble() ?: 0.0
            gold.value = it?.stats?.gp ?: 0.0
        }

        view.post { setGridSpanCount(view.width) }

        lifecycleScope.launchCatching {
            inventoryRepository.getOwnedItems()
                .collect { adapter?.setOwnedItems(it) }
        }

        lifecycleScope.launchCatching {
            contentRepository.getWorldState()
                .collect {
                    adapter?.shopSpriteSuffix = it.findNpcImageSuffix()
                }
        }

        lifecycleScope.launchCatching {
            inventoryRepository.getInAppRewards()
                .map { rewards -> rewards.map { it.key } }
                .collect { adapter?.setPinnedItemKeys(it) }
        }

        Analytics.sendNavigationEvent("$shopIdentifier screen")
    }

    open fun initializeCurrencyViews() {
        currencyView.setContent {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                gems.value?.let { CurrencyText(currency = "gems", value = it) }
                gold.value?.let { CurrencyText(currency = "gold", value = it) }
            }
        }
    }

    private fun showClassChangeDialog(classIdentifier: String) {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val user = userViewModel.user.value ?: return@launch
            context?.let { context ->
                if (user.gemCount <= 2) {
                    val dialog = mainActivity?.let { InsufficientGemsDialog(it, 3) }
                    Analytics.sendEvent("show insufficient gems modal", EventCategory.BEHAVIOUR, HitType.EVENT, mapOf("reason" to "class change"))
                    dialog?.show()
                    return@launch
                }
                if (user.flags?.classSelected == true && user.preferences?.disableClasses == false) {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(getString(R.string.change_class_selected_confirmation, classIdentifier))
                    alert.setMessage(getString(R.string.change_class_equipment_warning))
                    alert.addButton(R.string.choose_class, true) { _, _ ->
                        val dialog =
                            HabiticaProgressDialog.show(
                                requireActivity(),
                                getString(R.string.changing_class_progress),
                                300
                            )
                        lifecycleScope.launch(Dispatchers.Main) {
                            userRepository.changeClass(classIdentifier)
                            dialog.dismiss()
                            displayClassChanged(classIdentifier)
                            loadMarketGear()
                        }
                    }
                    alert.addButton(R.string.close, false)
                    alert.show()
                } else {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(getString(R.string.class_confirmation, classIdentifier))
                    alert.addButton(R.string.choose_class, true) { _, _ ->
                        val dialog =
                            HabiticaProgressDialog.show(
                                requireActivity(),
                                getString(R.string.changing_class_progress),
                                300
                            )
                        lifecycleScope.launch(Dispatchers.Main) {
                            userRepository.changeClass(classIdentifier)
                            dialog.dismiss()
                            displayClassChanged(classIdentifier)
                            loadMarketGear()
                        }
                    }
                    alert.addButton(R.string.close, false)
                    alert.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (shop == null) {
            loadShopInventory()
        }
    }

    private fun loadShopInventory() {
        val shopUrl =
            when (this.shopIdentifier) {
                Shop.MARKET -> "market"
                Shop.QUEST_SHOP -> "quests"
                Shop.TIME_TRAVELERS_SHOP -> "time-travelers"
                Shop.SEASONAL_SHOP -> "seasonal"
                Shop.CUSTOMIZATIONS -> "customizations"
                else -> ""
            }
        lifecycleScope.launchCatching({
            binding?.recyclerView?.state = RecyclerViewState.FAILED
        }) {
            val newShop = inventoryRepository.retrieveShopInventory(shopUrl) ?: return@launchCatching
            when (newShop.identifier) {
                Shop.MARKET -> {
                    val user = userViewModel.user.value
                    val specialCategory = ShopCategory()
                    specialCategory.text = getString(R.string.special)
                    val item = ShopItem.makeGemItem(context?.resources)
                    if (user?.isSubscribed == true) {
                        item.limitedNumberLeft = user.purchased?.plan?.numberOfGemsLeft
                    } else {
                        item.limitedNumberLeft = -1
                    }
                    specialCategory.items.add(item)
                    specialCategory.items.add(ShopItem.makeFortifyItem(context?.resources))
                    if (user?.flags?.rebirthEnabled == true) {
                        specialCategory.items.add(ShopItem.makeRebirthItem(context?.resources, user))
                    }
                    newShop.categories.add(specialCategory)
                }

                Shop.TIME_TRAVELERS_SHOP -> {
                    formatTimeTravelersShop(newShop)
                }

                Shop.SEASONAL_SHOP -> {
                    newShop.categories.sortWith(
                        compareBy<ShopCategory> { it.items.firstOrNull()?.currency != "gold" }
                            .thenByDescending { if (it.identifier == "quests") 10000 else findReleaseYear(it.items.firstOrNull()?.key ?: "") }
                            .thenBy { it.items.firstOrNull()?.locked }
                    )
                }
            }
            newShop.categories.forEach { category ->
                if (category.endDate == null) {
                    category.endDate = category.items.firstOrNull { it.availableUntil != null }?.availableUntil
                }
            }
            shop = newShop
            adapter?.shopIdentifier = shopIdentifier
            adapter?.setShop(newShop)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun findReleaseYear(key: String): Int {
        val result = key.filter { it.isDigit() }
        return if (result.isEmpty()) {
            2014
        } else {
            result.toInt()
        }
    }

    private fun formatTimeTravelersShop(shop: Shop): Shop {
        val newCategories = mutableListOf<ShopCategory>()
        for (category in shop.categories) {
            if (category.pinType != "mystery_set") {
                newCategories.add(category)
            } else {
                val newCategory = newCategories.find { it.identifier == "mystery_sets" } ?: ShopCategory()
                if (newCategory.identifier.isEmpty()) {
                    newCategory.identifier = "mystery_sets"
                    newCategory.text = getString(R.string.mystery_sets)
                    newCategories.add(newCategory)
                }
                val item = category.items.firstOrNull() ?: continue
                item.key = category.identifier
                item.text = category.text
                item.imageName = "shop_set_mystery_${item.key}"
                item.pinType = "mystery_set"
                item.path = "mystery.${item.key}"
                newCategory.items.add(item)
            }
        }
        val mysterySetCategory = newCategories.find { it.identifier == "mystery_sets" } ?: ShopCategory()
        if (mysterySetCategory.identifier.isEmpty()) {
            mysterySetCategory.identifier = "mystery_sets"
            mysterySetCategory.text = getString(R.string.mystery_sets)
            newCategories.add(mysterySetCategory)
        }
        shop.categories = newCategories
        return shop
    }

    private fun loadMarketGear() {
        lifecycleScope.launchCatching {
            val shop = inventoryRepository.retrieveMarketGear()
            val equipment =
                inventoryRepository.getOwnedEquipment()
                    .map { equipment -> equipment.map { it.key } }.firstOrNull()
            for (category in shop?.categories ?: emptyList()) {
                val items =
                    category.items.asSequence().filter {
                        equipment?.contains(it.key) == false
                    }.sortedBy { it.locked }.toList()
                category.items.clear()
                category.items.addAll(items)
            }
            gearCategories = shop?.categories
            adapter?.gearCategories = shop?.categories ?: mutableListOf()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SHOP_IDENTIFIER_KEY, this.shopIdentifier)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        context?.let { context ->
            val itemWidth: Float = context.resources.getDimension(R.dimen.reward_width)

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
        layoutManager?.requestLayout()
    }

    private fun displayClassChanged(selectedClass: String) {
        context?.let { context ->
            val alert = HabiticaAlertDialog(context)
            alert.setMessage(getString(R.string.class_changed_description, selectedClass))
            alert.addButton(getString(R.string.complete_tutorial), true) { _, _ -> alert.dismiss() }
            alert.show()
        }
    }

    companion object {
        private const val SHOP_IDENTIFIER_KEY = "SHOP_IDENTIFIER_KEY"
    }
}
