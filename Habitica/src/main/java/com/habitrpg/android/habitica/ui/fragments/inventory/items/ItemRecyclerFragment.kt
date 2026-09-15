package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentItemsBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.skills.SkillDialogBottomSheetFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.viewmodels.inventory.items.ItemListViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.OpenedMysteryitemDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.extensions.observeOnce
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ItemRecyclerFragment :
    BaseFragment<FragmentItemsBinding>(),
    SwipeRefreshLayout.OnRefreshListener {
    val viewModel: ItemListViewModel by viewModels()

    @Inject
    lateinit var userViewModel: MainUserViewModel

    var user: User? = null
    var adapter: ItemRecyclerAdapter? = null
    var itemType: String?
        get() = arguments?.getString(ARG_ITEM_TYPE)
        set(value) {
            val args = arguments ?: Bundle().also { arguments = it }
            args.putString(ARG_ITEM_TYPE, value)
        }
    var itemTypeText: String?
        get() = arguments?.getString(ARG_ITEM_TYPE_TEXT)
        set(value) {
            val args = arguments ?: Bundle().also { arguments = it }
            args.putString(ARG_ITEM_TYPE_TEXT, value)
        }
    private var selectedSpecialItem: SpecialItem? = null
    private var specialSkills: MutableList<Skill> = mutableListOf()
    internal var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null

    override var binding: FragmentItemsBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentItemsBinding = FragmentItemsBinding.inflate(inflater, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        getSpecialSkills()

        binding?.refreshLayout?.setOnRefreshListener(this)
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

        userViewModel.user.observeOnce(this) {
            if (it != null) {
                user = it
                adapter?.user = it
            }
        }

        binding?.titleTextView?.visibility = View.GONE
        setAdapter()
        this.loadItems()
    }

    private fun getSpecialSkills() {
        // Get special skills for description of special items
        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.specialSkills.collect { skills -> specialSkills = skills }
        }
    }

    private fun setAdapter() {
        val context = activity

        adapter = binding?.recyclerView?.adapter as? ItemRecyclerAdapter
        if (adapter == null) {
            context?.let {
                adapter = ItemRecyclerAdapter(context)
            }
            binding?.recyclerView?.adapter = adapter
        }
        adapter?.onUseSpecialItem = { specialItem ->
            val specialSkill = specialSkills.find { it.key == specialItem.key }
            if (specialSkill != null) {
                val skillIdentifier = "shop_"
                val bottomSheet =
                    SkillDialogBottomSheetFragment.newInstance(
                        skillTitle = specialSkill.text,
                        skillDescription = specialSkill.notes ?: "",
                        skillKey = specialSkill.key,
                        skillPath = skillIdentifier,
                        isTransformationItem = true,
                        onUseSkill = {
                            onSpecialItemSelected(specialItem)
                        },
                    )
                bottomSheet.show(childFragmentManager, "SkillDialogBottomSheet")
            }
        }
        adapter?.onSellItem = { item, ownedItem ->
            showSellItemConfirmation(item, ownedItem)
        }
        adapter?.onQuestInvitation = {
            viewModel.inviteToQuest(it)
        }
        adapter?.onOpenMysteryItem = {
            viewLifecycleOwner.lifecycleScope.launchCatching {
                val item = viewModel.openMysteryItem(user) ?: return@launchCatching
                val activity = activity as? MainActivity
                if (activity != null) {
                    val dialog = OpenedMysteryitemDialog(activity)
                    dialog.isCelebratory = true
                    dialog.setTitle(R.string.mystery_item_title)
                    dialog.binding.iconView.loadImage("shop_${item.key}")
                    dialog.binding.titleView.text = item.text
                    dialog.binding.descriptionView.text = item.notes
                    dialog.addButton(R.string.equip, true) { _, _ ->
                        item.key?.let { mysteryItem ->
                            viewModel.equip(
                                "equipped",
                                mysteryItem,
                            )
                        }
                    }
                    dialog.addCloseButton()
                    dialog.enqueue()
                }
            }
        }
        adapter?.onStartHatching = { showHatchingDialog(it) }
        adapter?.onHatchPet = { pet, egg -> hatchPet(pet, egg) }
        adapter?.onCreateNewParty = { createNewParty() }
        adapter?.itemType = itemType ?: ""
        adapter?.itemText =
            (if (itemType == "hatchingPotions") context?.getString(R.string.potions) else itemTypeText)
                ?: ""
        adapter?.onOpenShop = {
            if (itemType == "quests") {
                MainNavigationController.navigate(R.id.questShopFragment)
            } else {
                openMarket()
            }
        }
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

    override fun onRefresh() {
        binding?.refreshLayout?.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.refreshUser()
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun hatchPet(
        potion: HatchingPotion,
        egg: Egg,
    ) {
        (activity as? BaseActivity)?.let {
            viewModel.hatchPet(potion, egg, it)
        }
    }

    private fun createNewParty() {
        val alert = context?.let { HabiticaAlertDialog(it) }
        alert?.setTitle(R.string.quest_party_required_title)
        alert?.setMessage(R.string.quest_party_required_description)
        alert?.addButton(R.string.create_new_party, true, false) { _, _ ->
            viewLifecycleOwner.lifecycleScope.launchCatching {
                user?.let {
                    viewModel.createParty(
                        it,
                        getString(R.string.usernames_party, user?.profile?.name),
                    )
                }
            }
        }
        alert?.addButton(R.string.close, false) { _, _ ->
            alert.dismiss()
        }
        alert?.show()
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

    private fun onSpecialItemSelected(specialItem: SpecialItem) {
        selectedSpecialItem = specialItem
        val intent = Intent(activity, SkillMemberActivity::class.java)
        memberSelectionResult.launch(intent)
    }

    private val memberSelectionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                useSpecialItem(selectedSpecialItem, it.data?.getStringExtra("member_id"))
            }
        }

    private fun useSpecialItem(
        specialItem: SpecialItem?,
        memberID: String? = null,
    ) {
        if (specialItem == null || memberID == null) {
            return
        }
        lifecycleScope.launchCatching {
            viewModel.useSkill(specialItem.key, specialItem.target, memberID)
            displaySpecialItemResult(specialItem)
        }
    }

    private fun displaySpecialItemResult(specialItem: SpecialItem?) {
        if (!isAdded) return

        val activity = activity as? MainActivity
        activity?.let {
            HabiticaSnackbar.showSnackbar(
                it.snackbarContainer,
                context?.getString(R.string.used_skill_without_mana, specialItem?.text),
                HabiticaSnackbar.SnackbarDisplayType.BLUE,
            )
        }

        loadItems()
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
            isDestructive = true,
        ) { _, _ ->
            lifecycleScope.launchCatching {
                viewModel.sellItem(ownedItem)
            }
        }
        dialog.addCancelButton()
        dialog.show()
    }

    companion object {
        const val ARG_ITEM_TYPE = "CLASS_TYPE_KEY"
        const val ARG_ITEM_TYPE_TEXT = "CLASS_TYPE_TEXT_KEY"
    }
}
