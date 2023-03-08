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
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.CurrencyText
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.RecyclerViewState
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.flow.filter
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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                val dialog = PurchaseDialog(requireContext(), userRepository, inventoryRepository, item)
                dialog.shopIdentifier = shopIdentifier
                dialog.isPinned = isPinned
                dialog.onGearPurchased = {
                    loadShopInventory()
                    if (Shop.MARKET == shopIdentifier) {
                        loadMarketGear()
                    }
                }
                dialog.show()
            }
            adapter?.context = context
            binding?.recyclerView?.adapter = adapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
            adapter?.changeClassEvents = {
                showClassChangeDialog(it)
            }
        }

        if (binding?.recyclerView?.layoutManager == null) {
            layoutManager = GridLayoutManager(context, 2)
            layoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if ((adapter?.getItemViewType(position) ?: 0) < 3) {
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

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.getGroup(Group.TAVERN_ID)
                .filter { it?.hasActiveQuest == true }
                .filter { group -> group?.quest?.rageStrikes?.any { it.key == shopIdentifier } ?: false }
                .filter { group -> group?.quest?.rageStrikes?.filter { it.key == shopIdentifier }?.get(0)?.wasHit == true }
                .collect {
                    adapter?.shopSpriteSuffix = "_" + it?.quest?.key
                }
        }

        view.post { setGridSpanCount(view.width) }

        context?.let { analyticsManager.logEvent("open_shop", bundleOf(Pair("shopIdentifier", shopIdentifier))) }
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
        context?.let { context ->
            val alert = HabiticaAlertDialog(context)
            alert.setTitle(getString(R.string.class_confirmation_price, classIdentifier, 3))
            alert.addButton(R.string.choose_class, true) { _, _ ->
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    userRepository.changeClass(classIdentifier)
                }
            }
            alert.addButton(R.string.dialog_go_back, false)
            alert.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadShopInventory()
    }

    private fun loadShopInventory() {
        val shopUrl = when (this.shopIdentifier) {
            Shop.MARKET -> "market"
            Shop.QUEST_SHOP -> "quests"
            Shop.TIME_TRAVELERS_SHOP -> "time-travelers"
            Shop.SEASONAL_SHOP -> "seasonal"
            else -> ""
        }
        lifecycleScope.launchCatching({
            binding?.recyclerView?.state = RecyclerViewState.FAILED
        }) {
            val shop1 = inventoryRepository.retrieveShopInventory(shopUrl) ?: return@launchCatching
            if (shop1.identifier == Shop.MARKET) {
                val user = userViewModel.user.value
                val specialCategory = ShopCategory()
                specialCategory.text = getString(R.string.special)
                if (user?.isValid == true && user.purchased?.plan?.isActive == true) {
                    val item = ShopItem.makeGemItem(context?.resources)
                    item.limitedNumberLeft = user.purchased?.plan?.numberOfGemsLeft
                    specialCategory.items.add(item)
                }
                specialCategory.items.add(ShopItem.makeFortifyItem(context?.resources))
                shop1.categories.add(specialCategory)
            }
            when (shop1.identifier) {
                Shop.TIME_TRAVELERS_SHOP -> {
                    formatTimeTravelersShop(shop1)
                }
                Shop.SEASONAL_SHOP -> {
                    shop1.categories.sortWith(
                        compareBy<ShopCategory> { it.items.firstOrNull()?.currency != "gold" }
                            .thenByDescending { it.items.firstOrNull()?.event?.end }
                            .thenBy { it.items.firstOrNull()?.locked }
                    )
                }
            }
            shop = shop1
            adapter?.setShop(shop1)
            binding?.refreshLayout?.isRefreshing = false
        }

        lifecycleScope.launchCatching {
            inventoryRepository.getOwnedItems()
                .collect { adapter?.setOwnedItems(it) }
        }
        lifecycleScope.launchCatching {
            inventoryRepository.getInAppRewards()
                .map { rewards -> rewards.map { it.key } }
                .collect { adapter?.setPinnedItemKeys(it) }
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
        shop.categories = newCategories
        return shop
    }

    private fun loadMarketGear() {
        lifecycleScope.launchCatching {
            val shop = inventoryRepository.retrieveMarketGear()
            inventoryRepository.getOwnedEquipment()
                .map { equipment -> equipment.map { it.key } }
                .collect { equipment ->
                    for (category in shop?.categories ?: emptyList()) {
                        val items = category.items.asSequence().filter {
                            !equipment.contains(it.key)
                        }.sortedBy { it.locked }.toList()
                        category.items.clear()
                        category.items.addAll(items)
                    }
                    gearCategories = shop?.categories
                    adapter?.gearCategories = shop?.categories ?: mutableListOf()
                }
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

    companion object {
        private const val SHOP_IDENTIFIER_KEY = "SHOP_IDENTIFIER_KEY"
    }
}
