package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentItemsBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.EmptyItem
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.loadImage
import com.habitrpg.android.habitica.ui.views.dialogs.OpenedMysteryitemDialog
import javax.inject.Inject

class ItemRecyclerFragment : BaseFragment<FragmentItemsBinding>(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    var adapter: ItemRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    var user: User? = null
    internal var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null

    override var binding: FragmentItemsBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentItemsBinding {
        return FragmentItemsBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.recyclerView?.emptyItem = EmptyItem(
            getString(R.string.empty_items, itemTypeText ?: itemType),
            null,
            null,
            if (itemType == "special") null else getString(R.string.open_shop)
        ) {
            if (itemType == "quests") {
                MainNavigationController.navigate(R.id.questShopFragment)
            } else {
                openMarket()
            }
        }

        val context = activity

        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = binding?.recyclerView?.adapter as? ItemRecyclerAdapter
        if (adapter == null) {
            context?.let {
                adapter = ItemRecyclerAdapter(context)
            }
            binding?.recyclerView?.adapter = adapter

            adapter?.let { adapter ->
                compositeSubscription.add(adapter.getSellItemFlowable()
                        .flatMap { item -> inventoryRepository.sellItem(item) }
                        .subscribe({ }, RxErrorHandler.handleEmptyError()))

                compositeSubscription.add(adapter.getQuestInvitationFlowable()
                        .flatMap { quest -> inventoryRepository.inviteToQuest(quest) }
                        .flatMap { socialRepository.retrieveGroup("party") }
                        .subscribe({
                            MainNavigationController.navigate(R.id.partyFragment)
                        }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.getOpenMysteryItemFlowable()
                        .flatMap { inventoryRepository.openMysteryItem(user) }
                        .doOnNext {
                            val activity = activity as? MainActivity
                            if (activity != null) {
                                val dialog = OpenedMysteryitemDialog(activity)
                                dialog.isCelebratory = true
                                dialog.setTitle(R.string.mystery_item_title)
                                dialog.binding.iconView.loadImage("shop_${it.key}")
                                dialog.binding.titleView.text = it.text
                                dialog.binding.descriptionView.text = it.notes
                                dialog.addButton(R.string.equip, true) { _, _ ->
                                    inventoryRepository.equip(user, "equipped", it.key ?: "").subscribe( {}, RxErrorHandler.handleEmptyError())
                                }
                                dialog.addCloseButton()
                                dialog.enqueue()
                            }
                        }
                        .subscribe({ }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.startHatchingEvents.subscribeWithErrorHandler { showHatchingDialog(it) })
                compositeSubscription.add(adapter.hatchPetEvents.subscribeWithErrorHandler { hatchPet(it.first, it.second) })
            }
        }
        activity?.let {
            binding?.recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(it, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
        }

        binding?.titleTextView?.visibility = View.GONE
        binding?.footerTextView?.visibility = View.GONE
        binding?.openMarketButton?.visibility = View.GONE

        binding?.openMarketButton?.setOnClickListener {
            openMarket()
        }

        this.loadItems()
    }

    private fun showHatchingDialog(item: Item) {
        val fragment = ItemDialogFragment()
        if (item is Egg) {
            fragment.itemType = "hatchingPotions"
            fragment.hatchingItem = item
        } else {
            fragment.itemType = "eggs"
            fragment.hatchingItem = item
        }
        fragment.isHatching = true
        fragment.isFeeding = false
        parentFragmentManager.let { fragment.show(it, "hatchingDialog") }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ITEM_TYPE_KEY, this.itemType)
    }

    override fun onRefresh() {
        binding?.refreshLayout?.isRefreshing = true
        compositeSubscription.add(userRepository.retrieveUser(true, true)
                .doOnTerminate {
                    binding?.refreshLayout?.isRefreshing = false
                }.subscribe({ }, RxErrorHandler.handleEmptyError()))
    }

    private fun hatchPet(potion: HatchingPotion, egg: Egg) {
        (activity as? MainActivity)?.hatchPet(potion, egg)
    }

    private fun loadItems() {
        val itemClass: Class<out Item> = when (itemType) {
            "eggs" -> Egg::class.java
            "hatchingPotions" -> HatchingPotion::class.java
            "food" -> Food::class.java
            "quests" -> QuestContent::class.java
            "special" -> SpecialItem::class.java
            else -> Egg::class.java
        }
        itemType?.let { type ->
            compositeSubscription.add(inventoryRepository.getOwnedItems(type)
                .map { it.distinctBy { it.key } }
                .doOnNext { items ->
                    adapter?.data = items
                }
                .map { items -> items.mapNotNull { it.key } }
                .flatMap { inventoryRepository.getItems(itemClass, it.toTypedArray()) }
                .map {
                    val itemMap = mutableMapOf<String, Item>()
                    for (item in it) {
                        itemMap[item.key] = item
                    }
                    itemMap
                }
                .subscribe({ items ->
                adapter?.items = items
            }, RxErrorHandler.handleEmptyError()))
        }

        compositeSubscription.add(inventoryRepository.getPets().subscribe({ adapter?.setExistingPets(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(inventoryRepository.getOwnedPets()
                .map { ownedMounts ->
                    val mountMap = mutableMapOf<String, OwnedPet>()
                    ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                    return@map mountMap
                }
                .subscribe({ adapter?.setOwnedPets(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun openMarket() {
        MainNavigationController.navigate(R.id.marketFragment)
    }

    companion object {

        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
