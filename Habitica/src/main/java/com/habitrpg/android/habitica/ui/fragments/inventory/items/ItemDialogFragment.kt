package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentItemsDialogBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseDialogFragment
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.OpenedMysteryitemDialog
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemDialogFragment : BaseDialogFragment<FragmentItemsDialogBinding>() {

    var parentSubscription: CompositeDisposable? = null

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var hatchPetUseCase: HatchPetUseCase
    @Inject
    lateinit var feedPetUseCase: FeedPetUseCase
    @Inject
    lateinit var userViewModel: MainUserViewModel

    var adapter: ItemRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    var isHatching: Boolean = false
    var isFeeding: Boolean = false
    internal var hatchingItem: Item? = null
    var feedingPet: Pet? = null
    var user: User? = null
    internal var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null

    override var binding: FragmentItemsDialogBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentItemsDialogBinding {
        return FragmentItemsDialogBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        when {
            this.isHatching -> {
                dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            }
            this.isFeeding -> {
                dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            }
            else -> {
            }
        }

        binding?.recyclerView?.isNestedScrollingEnabled = true

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView?.emptyItem = EmptyItem(
            getString(R.string.empty_items, itemTypeText ?: itemType),
            getString(R.string.open_market)
        ) {
            openMarket()
        }

        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager
        activity?.let {
            binding?.recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(it, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        setAdapter()
        userViewModel.user.observeOnce(this) {
            if (it != null) {
                user = it
                adapter?.user = it
            }
        }

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
        }

        when {
            this.isHatching -> {
                binding?.titleTextView?.text = getString(R.string.hatch_with, this.hatchingItem?.text)
                binding?.titleTextView?.visibility = View.VISIBLE
                binding?.footerTextView?.text = getString(R.string.hatching_market_info)
                binding?.footerTextView?.visibility = View.VISIBLE
                binding?.openMarketButton?.visibility = View.VISIBLE
            }
            this.isFeeding -> {
                binding?.titleTextView?.text = getString(R.string.dialog_feeding, this.feedingPet?.text)
                binding?.titleTextView?.visibility = View.VISIBLE
                binding?.footerTextView?.text = getString(R.string.feeding_market_info)
                binding?.footerTextView?.visibility = View.VISIBLE
                binding?.openMarketButton?.visibility = View.VISIBLE
            }
            else -> {
                binding?.titleTextView?.visibility = View.GONE
                binding?.footerTextView?.visibility = View.GONE
                binding?.openMarketButton?.visibility = View.GONE
            }
        }

        binding?.openMarketButton?.setOnClickListener {
            dismiss()
            openMarket()
        }

        this.loadItems()
    }

    private fun setAdapter() {
        val context = activity

        adapter = binding?.recyclerView?.adapter as? ItemRecyclerAdapter
        if (adapter == null) {
            context?.let {
                adapter = ItemRecyclerAdapter(context)
                adapter?.isHatching = this.isHatching
                adapter?.isFeeding = this.isFeeding
                adapter?.fragment = this
            }
            if (this.hatchingItem != null) {
                adapter?.hatchingItem = this.hatchingItem
            }
            if (this.feedingPet != null) {
                adapter?.feedingPet = this.feedingPet
            }
            binding?.recyclerView?.adapter = adapter

            adapter?.let { adapter ->
                compositeSubscription.add(
                    adapter.getSellItemFlowable()
                        .flatMap { item -> inventoryRepository.sellItem(item) }
                        .subscribe({ }, RxErrorHandler.handleEmptyError())
                )

                compositeSubscription.add(
                    adapter.getQuestInvitationFlowable()
                        .flatMap { quest -> inventoryRepository.inviteToQuest(quest) }
                        .flatMap { socialRepository.retrieveGroup("party") }
                        .subscribe(
                            {
                                if (isModal) {
                                    dismiss()
                                } else {
                                    MainNavigationController.navigate(R.id.partyFragment)
                                }
                            },
                            RxErrorHandler.handleEmptyError()
                        )
                )
                compositeSubscription.add(
                    adapter.getOpenMysteryItemFlowable()
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
                                    inventoryRepository.equip("equipped", it.key ?: "").subscribe({}, RxErrorHandler.handleEmptyError())
                                }
                                dialog.addCloseButton()
                                dialog.enqueue()
                            }
                        }
                        .subscribe({ }, RxErrorHandler.handleEmptyError())
                )
                compositeSubscription.add(adapter.hatchPetEvents.subscribeWithErrorHandler { hatchPet(it.first, it.second) })
                compositeSubscription.add(adapter.feedPetEvents.subscribeWithErrorHandler { feedPet(it) })
            }
        }
    }

    private fun feedPet(food: Food) {
        val pet = feedingPet ?: return
        val activity = activity ?: return
        parentSubscription?.add(
            feedPetUseCase.observable(
                FeedPetUseCase.RequestValues(
                    pet, food,
                    activity
                )
            ).subscribeWithErrorHandler {}
        )
    }

    override fun onResume() {
        if ((this.isHatching || this.isFeeding) && dialog?.window != null) {
            val params = dialog?.window?.attributes
            params?.width = ViewGroup.LayoutParams.MATCH_PARENT
            params?.verticalMargin = 60f
            dialog?.window?.attributes = params
        }

        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ITEM_TYPE_KEY, this.itemType)
    }

    private fun hatchPet(potion: HatchingPotion, egg: Egg) {
        dismiss()
        val activity = activity ?: return
        parentSubscription?.add(
            hatchPetUseCase.observable(
                HatchPetUseCase.RequestValues(
                    potion, egg,
                    activity
                )
            ).subscribeWithErrorHandler {}
        )
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
                lifecycleScope.launch {
                    inventoryRepository.getOwnedItems(type)
                        .onEach { items ->
                            val filteredItems = if (isFeeding) {
                                items.filter { it.key != "Saddle" }
                            } else {
                                items
                            }
                            adapter?.data = filteredItems
                        }
                        .map { items -> items.mapNotNull { it.key } }
                        .map { inventoryRepository.getItemsFlowable(itemClass, it.toTypedArray()).firstOrNull() }
                        .collect {
                            val itemMap = mutableMapOf<String, Item>()
                            for (item in it ?: emptyList()) {
                                itemMap[item.key] = item
                            }
                            adapter?.items = itemMap
                        }

                }
                lifecycleScope.launch {
                    inventoryRepository.getPets().collect { adapter?.setExistingPets(it) }
                }
                lifecycleScope.launch {
                    inventoryRepository.getOwnedPets().map { ownedPets ->
                        val petMap = mutableMapOf<String, OwnedPet>()
                        ownedPets.forEach { petMap[it.key ?: ""] = it }
                        return@map petMap
                    }.collect { adapter?.setOwnedPets(it) }
                }
        }
    }

    private fun openMarket() {
        MainNavigationController.navigate(R.id.marketFragment)
    }

    companion object {
        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
