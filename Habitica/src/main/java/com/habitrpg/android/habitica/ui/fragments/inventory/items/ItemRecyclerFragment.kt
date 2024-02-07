package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentItemsBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.common.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.OpenedMysteryitemDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ItemRecyclerFragment : BaseFragment<FragmentItemsBinding>(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    internal lateinit var hatchPetUseCase: HatchPetUseCase

    @Inject
    lateinit var userViewModel: MainUserViewModel

    var user: User? = null
    var adapter: ItemRecyclerAdapter? = null
    var itemType: String? = null
    var transformationItems: MutableList<OwnedItem> = mutableListOf()
    var itemTypeText: String? = null
    private var selectedSpecialItem: SpecialItem? = null
    internal var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null

    override var binding: FragmentItemsBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentItemsBinding {
        return FragmentItemsBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
            this.itemTypeText = savedInstanceState.getString(ITEM_TYPE_TEXT_KEY, "")
        }

        binding?.refreshLayout?.setOnRefreshListener(this)
        val buttonMethod = {
            Analytics.sendEvent("Items CTA tap", EventCategory.BEHAVIOUR, HitType.EVENT, mapOf(
                "area" to "empty",
                "type" to (itemType ?: "")
            ))
            if (itemType == "quests") {
                MainNavigationController.navigate(R.id.questShopFragment)
            } else {
                openMarket()
            }
        }
        binding?.recyclerView?.emptyItem = EmptyItem(
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
            if (itemType == "special") null else buttonMethod)

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

    private fun setAdapter() {
        val context = activity

        adapter = binding?.recyclerView?.adapter as? ItemRecyclerAdapter
        if (adapter == null) {
            context?.let {
                adapter = ItemRecyclerAdapter(context)
            }
            binding?.recyclerView?.adapter = adapter
        }
        adapter?.onUseSpecialItem = { onSpecialItemSelected(it) }
        adapter?.onSellItem = {
            lifecycleScope.launchCatching {
                inventoryRepository.sellItem(it)
            }
        }
        adapter?.onQuestInvitation = {
            lifecycleScope.launchCatching {
                inventoryRepository.inviteToQuest(it)
                MainNavigationController.navigate(R.id.partyFragment)
            }
        }
        adapter?.onOpenMysteryItem = {
            lifecycleScope.launchCatching {
                val item = inventoryRepository.openMysteryItem(user) ?: return@launchCatching
                val activity = activity as? MainActivity
                if (activity != null) {
                    val dialog = OpenedMysteryitemDialog(activity)
                    dialog.isCelebratory = true
                    dialog.setTitle(R.string.mystery_item_title)
                    dialog.binding.iconView.loadImage("shop_${item.key}")
                    dialog.binding.titleView.text = item.text
                    dialog.binding.descriptionView.text = item.notes
                    dialog.addButton(R.string.equip, true) { _, _ ->
                        lifecycleScope.launchCatching {
                            item.key?.let { mysteryItem -> inventoryRepository.equip("equipped", mysteryItem) }
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
        adapter?.itemText = (if (itemType == "hatchingPotions") context?.getString(R.string.potions) else itemTypeText) ?: ""
        adapter?.onOpenShop = {
            Analytics.sendEvent("Items CTA tap", EventCategory.BEHAVIOUR, HitType.EVENT, mapOf(
                "area" to "bottom",
                "type" to (itemType ?: "")
            ))
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ITEM_TYPE_KEY, this.itemType)
        outState.putString(ITEM_TYPE_TEXT_KEY, this.itemTypeText)
    }

    override fun onRefresh() {
        binding?.refreshLayout?.isRefreshing = true
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun hatchPet(potion: HatchingPotion, egg: Egg) {
        (activity as? BaseActivity)?.let {
            lifecycleScope.launchCatching {
                hatchPetUseCase.callInteractor(
                    HatchPetUseCase.RequestValues(
                        potion,
                        egg,
                        it
                    )
                )
            }
        }
    }

    private fun createNewParty() {
        val alert = context?.let { HabiticaAlertDialog(it) }
        alert?.setTitle(R.string.quest_party_required_title)
        alert?.setMessage(R.string.quest_party_required_description)
        alert?.addButton(R.string.create_new_party, true, false) { _, _ ->
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.createGroup(
                    getString(R.string.usernames_party, user?.profile?.name),
                    "",
                    user?.id,
                    "party",
                    "",
                    false
                )
                val user = userRepository.retrieveUser(false, true)
                if (user?.hasParty == true) {
                    val party = socialRepository.retrieveGroup("party")
                    socialRepository.retrievePartyMembers(party?.id ?: "", true)
                    MainNavigationController.navigate(
                        R.id.partyFragment,
                        bundleOf(Pair("partyID", user.party?.id))
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
        val itemClass: Class<out Item> = when (itemType) {
            "eggs" -> Egg::class.java
            "hatchingPotions" -> HatchingPotion::class.java
            "food" -> Food::class.java
            "quests" -> QuestContent::class.java
            "special" -> SpecialItem::class.java
            else -> Egg::class.java
        }
        itemType?.let { type ->
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                inventoryRepository.getOwnedItems(type)
                    .onEach { items ->
                        adapter?.data = items
                    }
                    .map { items -> items.mapNotNull { it.key } }
                    .map {
                        inventoryRepository.getItems(itemClass, it.toTypedArray()).firstOrNull()
                    }
                    .collect {
                        val itemMap = mutableMapOf<String, Item>()
                        for (item in it ?: emptyList()) {
                            itemMap[item.key] = item
                        }
                        adapter?.items = itemMap
                    }
            }
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                inventoryRepository.getPets().collect { adapter?.setExistingPets(it) }
            }
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
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

    private fun useSpecialItem(specialItem: SpecialItem?, memberID: String? = null) {
        if (specialItem == null || memberID == null) {
            return
        }
        lifecycleScope.launchCatching {
            userRepository.useSkill(specialItem.key, specialItem.target, memberID)
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
                HabiticaSnackbar.SnackbarDisplayType.BLUE
            )
        }

        loadItems()
    }

    companion object {

        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
        private const val ITEM_TYPE_TEXT_KEY = "CLASS_TYPE_TEXT_KEY"
    }
}
