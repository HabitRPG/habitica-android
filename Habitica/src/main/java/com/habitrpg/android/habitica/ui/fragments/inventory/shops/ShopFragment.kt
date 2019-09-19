package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.GearPurchasedEvent
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class ShopFragment : BaseFragment() {

    var adapter: ShopRecyclerAdapter? = null
    var shopIdentifier: String? = null
    var user: User? = null
    var shop: Shop? = null
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var configManager: AppConfigManager

    private var layoutManager: GridLayoutManager? = null

    private var gearCategories: MutableList<ShopCategory>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_recyclerview, container, false)
    }

    override fun onDestroyView() {
        userRepository.close()
        inventoryRepository.close()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.setBackgroundResource(R.color.white)

        adapter = recyclerView.adapter as? ShopRecyclerAdapter
        if (adapter == null) {
            adapter = ShopRecyclerAdapter()
            adapter?.context = context
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = SafeDefaultItemAnimator()
        }

        if (recyclerView.layoutManager == null) {
            layoutManager = GridLayoutManager(context, 2)
            layoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter?.getItemViewType(position) ?: 0 < 3) {
                        layoutManager?.spanCount ?: 1
                    } else {
                        1
                    }
                }
            }
            recyclerView.layoutManager = layoutManager
        }

        if (savedInstanceState != null) {
            this.shopIdentifier = savedInstanceState.getString(SHOP_IDENTIFIER_KEY, "")
        }

        adapter?.selectedGearCategory = user?.stats?.habitClass ?: ""

        if (shop == null) {
            loadShopInventory()
        } else {
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

        adapter?.user = user

        compositeSubscription.add(socialRepository.getGroup(Group.TAVERN_ID)
                .filter { it.hasActiveQuest }
                .filter { group -> group.quest?.rageStrikes?.any { it.key == shopIdentifier } ?: false }
                .filter { group -> group.quest?.rageStrikes?.filter { it.key == shopIdentifier }?.get(0)?.wasHit == true }
                .subscribe(Consumer {
                    adapter?.shopSpriteSuffix = "_"+it.quest?.key
                }, RxErrorHandler.handleEmptyError()))

        view.post { setGridSpanCount(view.width) }
    }

    private fun loadShopInventory() {
        val shopUrl = when (this.shopIdentifier) {
            Shop.MARKET -> "market"
            Shop.QUEST_SHOP -> "quests"
            Shop.TIME_TRAVELERS_SHOP -> "time-travelers"
            Shop.SEASONAL_SHOP -> "seasonal"
            else -> ""
        }
        compositeSubscription.add(this.inventoryRepository.retrieveShopInventory(shopUrl)
                .map { shop1 ->
                    if (shop1.identifier == Shop.MARKET) {
                        val user = user
                        if (user?.isValid == true && user.purchased?.plan?.isActive == true) {
                            val specialCategory = ShopCategory()
                            specialCategory.text = getString(R.string.special)
                            val item = ShopItem.makeGemItem(context?.resources)
                            item.limitedNumberLeft = user.purchased?.plan?.numberOfGemsLeft()
                            specialCategory.items.add(item)
                            shop1.categories.add(specialCategory)
                        }
                    }
                    if (shop1.identifier == Shop.TIME_TRAVELERS_SHOP) {
                        formatTimeTravelersShop(shop1)
                    } else {
                        shop1
                    }
                }
                .subscribe(Consumer {
                    this.shop = it
                    this.adapter?.setShop(it)
                }, RxErrorHandler.handleEmptyError()))



        compositeSubscription.add(this.inventoryRepository.getOwnedItems()
                .subscribe(Consumer { adapter?.setOwnedItems(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(this.inventoryRepository.getInAppRewards()
                .map { rewards -> rewards.map { it.key } }
                .subscribe(Consumer { adapter?.setPinnedItemKeys(it) }, RxErrorHandler.handleEmptyError()))
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
                 newCategory.items.add(item)
             }
        }
        shop.categories = newCategories
        return shop
    }

    private fun loadMarketGear() {
        compositeSubscription.add(inventoryRepository.retrieveMarketGear()
                .zipWith(inventoryRepository.getOwnedEquipment().map { equipment -> equipment.map { it.key } }, BiFunction<Shop, List<String?>, Shop> { shop, equipment ->
                    for (category in shop.categories) {
                        val items = category.items.asSequence().filter {
                            !equipment.contains(it.key)
                        }.sortedBy { it.locked }.toList()
                        category.items.clear()
                        category.items.addAll(items)
                    }
                    shop
                })
                .subscribe(Consumer<Shop> {
                    this.gearCategories = it.categories
                    adapter?.gearCategories = it.categories
                }, RxErrorHandler.handleEmptyError()))
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
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

    @Subscribe
    fun onItemPurchased(event: GearPurchasedEvent) {
        if (Shop.MARKET == shopIdentifier) {
            loadMarketGear()
        } else if (Shop.TIME_TRAVELERS_SHOP == shopIdentifier) {
            loadShopInventory()
        }
    }

}
