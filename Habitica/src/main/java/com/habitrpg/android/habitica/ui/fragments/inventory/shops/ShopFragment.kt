package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.GearPurchasedEvent
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RemoteConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import kotlinx.android.synthetic.main.fragment_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import rx.functions.Action1
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
    lateinit var configManager: RemoteConfigManager

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

        adapter = recyclerView.adapter as ShopRecyclerAdapter?
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
                .filter { it.quest?.rageStrikes?.any { it.key == shopIdentifier } }
                .filter { it.quest?.rageStrikes?.filter { it.key == shopIdentifier }?.get(0)?.wasHit == true }
                .subscribe(Action1 {
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
        this.inventoryRepository.retrieveShopInventory(shopUrl)
                .map { shop1 ->
                    if (shop1.identifier == Shop.MARKET) {
                        val user = user
                        if (user != null && user.isValid && user.purchased.plan.isActive) {
                            val specialCategory = ShopCategory()
                            specialCategory.text = getString(R.string.special)
                            val item = ShopItem.makeGemItem(context?.resources)
                            item.limitedNumberLeft = user.purchased.plan.numberOfGemsLeft()
                            specialCategory.items.add(item)
                            shop1.categories.add(specialCategory)
                        }
                    }
                    shop1
                }
                .subscribe(Action1 {
                    this.shop = it
                    this.adapter?.setShop(it)
                }, RxErrorHandler.handleEmptyError())



        user.notNull {
            compositeSubscription.add(this.inventoryRepository.getOwnedItems(it)
                    .subscribe(Action1 { adapter?.setOwnedItems(it) }, RxErrorHandler.handleEmptyError()))
        }
        compositeSubscription.add(this.inventoryRepository.inAppRewards
                .map<List<String>> { it.map { it.key } }
                .subscribe(Action1 { adapter?.setPinnedItemKeys(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun loadMarketGear() {
        inventoryRepository.retrieveMarketGear()
                .zipWith(inventoryRepository.ownedEquipment.first().map { it.map { it.key } }, { shop, equipment ->
                    for (category in shop.categories) {
                        val items = category.items.filter({
                            !equipment.contains(it.key)
                        }).sortedBy { it.locked }
                        category.items.clear()
                        category.items.addAll(items)
                    }
                    shop
                })
                .subscribe(Action1 {
                    this.gearCategories = it.categories
                    adapter?.gearCategories = it.categories
                }, RxErrorHandler.handleEmptyError())
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SHOP_IDENTIFIER_KEY, this.shopIdentifier)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        context.notNull { context ->
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
        }
    }

}
