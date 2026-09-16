package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentItemsDialogBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseDialogFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.viewmodels.inventory.items.ItemListViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.OpenedMysteryitemDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.extensions.observeOnce
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ItemDialogFragment : BaseDialogFragment<FragmentItemsDialogBinding>() {
    val viewModel: ItemListViewModel by viewModels()

    @Inject
    lateinit var userViewModel: MainUserViewModel

    var adapter: ItemRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    var isHatching: Boolean = false
    var isFeeding: Boolean = false
    var onFeedResult: ((FeedResponse?) -> Unit)? = null
    internal var hatchingItem: Item? = null
    var feedingPet: Pet? = null
    var user: User? = null
    internal var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null

    override var binding: FragmentItemsDialogBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentItemsDialogBinding = FragmentItemsDialogBinding.inflate(inflater, container, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (isHatching || this.isFeeding) {
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        binding?.recyclerView?.isNestedScrollingEnabled = true

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val buttonMethod = {
            if (itemType == "quests") {
                MainNavigationController.navigate(R.id.questShopFragment)
            } else {
                openMarket()
            }
        }
        binding?.recyclerView?.emptyItem =
            EmptyItem(
                getString(R.string.no_x, itemTypeText ?: itemType),
                when (itemType) {
                    "food" -> getString(R.string.empty_food_description)
                    "quests" -> getString(R.string.empty_quests_description)
                    "special" -> getString(R.string.empty_special_description_subscribed)
                    "eggs" -> getString(R.string.empty_eggs_description)
                    "hatchingPotions" -> getString(R.string.empty_potions_description)
                    else -> ""
                },
                when (itemType) {
                    "eggs" -> R.drawable.icon_eggs
                    "hatchingPotions" -> R.drawable.icon_hatchingpotions
                    "food" -> R.drawable.icon_food
                    "quests" -> R.drawable.icon_quests
                    "special" -> R.drawable.icon_special
                    else -> null
                },
                false,
                if (itemType == "special") null else buttonMethod,
            )

        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager
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
                binding?.titleTextView?.text =
                    getString(R.string.hatch_with, this.hatchingItem?.text)
                binding?.titleTextView?.visibility = View.VISIBLE
            }

            this.isFeeding -> {
                binding?.titleTextView?.text =
                    getString(R.string.dialog_feeding, this.feedingPet?.text)
                binding?.titleTextView?.visibility = View.VISIBLE
            }

            else -> {
                binding?.titleTextView?.visibility = View.GONE
            }
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
            adapter?.onSellItem = { item, ownedItem ->
                showSellItemConfirmation(item, ownedItem)
            }
            adapter?.onQuestInvitation = {
                lifecycleScope.launchCatching {
                    viewModel.inviteToQuest(it)
                }
            }
            adapter?.onOpenMysteryItem = {
                lifecycleScope.launchCatching {
                    val item = viewModel.openMysteryItem(user) ?: return@launchCatching
                    val activity = activity as? MainActivity
                    if (activity != null) {
                        val dialog = OpenedMysteryitemDialog(activity)
                        dialog.isCelebratory = true
                        dialog.setTitle(R.string.mystery_item_title)
                        dialog.binding.iconView.loadImage("shop_${it.key}")
                        dialog.binding.titleView.text = item.text
                        dialog.binding.descriptionView.text = item.notes
                        dialog.addButton(R.string.equip, true) { _, _ ->
                            viewModel.equip("equipped", it.key)
                        }
                        dialog.addCloseButton()
                        dialog.enqueue()
                    }
                }
            }
            adapter?.onHatchPet = { pet, egg -> hatchPet(pet, egg) }
            adapter?.onFeedPet = { food -> feedPet(food) }

            adapter?.onOpenShop = {
                if (itemType == "quests") {
                    MainNavigationController.navigate(R.id.questShopFragment)
                } else {
                    openMarket()
                }
            }
        }
    }

    private fun feedPet(food: Food) {
        val pet = feedingPet ?: return
        val activity = activity ?: return
        activity.lifecycleScope.launchCatching {
            val result = viewModel.feedPet(pet, food, activity)
            onFeedResult?.invoke(result)
        }
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

    private fun hatchPet(
        potion: HatchingPotion,
        egg: Egg,
    ) {
        dismiss()
        val activity = activity ?: return
        viewModel.hatchPet(potion, egg, activity)
    }

    private fun loadItems() {
        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.ownedItems.collect { adapter?.data = it }
        }
        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.items.collect {
                adapter?.items = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.pets.collect { adapter?.setExistingPets(it) }
        }
        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.ownedPets.collect { adapter?.setOwnedPets(it) }
        }
    }

    private fun openMarket() {
        MainNavigationController.navigate(R.id.marketFragment)
    }

    private fun showSellItemConfirmation(
        item: Item,
        ownedItem: OwnedItem,
    ) {
        val dialog = HabiticaAlertDialog(requireContext())
        dialog.setTitle(getString(R.string.sell_confirmation_title, item.text))
        dialog.addButton(
            getString(R.string.sell, item.value),
            isPrimary = true,
            isDestructive = true
        ) { _, _ ->
            lifecycleScope.launchCatching {
                viewModel.sellItem(ownedItem)
            }
        }
        dialog.addCancelButton()
        dialog.show()
    }

    companion object {
        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
