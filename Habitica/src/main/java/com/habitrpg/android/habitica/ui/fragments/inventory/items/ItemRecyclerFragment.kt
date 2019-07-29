package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.ShareEvent
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.*
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class ItemRecyclerFragment : BaseFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userRepository: UserRepository
    val recyclerView: RecyclerViewEmptySupport? by bindView(R.id.recyclerView)
    val emptyView: View? by bindView(R.id.emptyView)
    val emptyTextView: TextView? by bindView(R.id.empty_text_view)
    val titleView: TextView? by bindView(R.id.titleTextView)
    val footerView: TextView? by bindView(R.id.footerTextView)
    val openMarketButton: Button? by bindView(R.id.openMarketButton)
    val openEmptyMarketButton: Button? by bindView(R.id.openEmptyMarketButton)
    var adapter: ItemRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    var isHatching: Boolean = false
    var isFeeding: Boolean = false
    var hatchingItem: Item? = null
    var feedingPet: Pet? = null
    var user: User? = null
    internal var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_items, container, false)
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

        resetViews()

        recyclerView?.setEmptyView(emptyView)
        emptyTextView?.text = getString(R.string.empty_items, itemTypeText)

        val context = activity

        layoutManager = recyclerView?.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager

        if (layoutManager == null) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

            recyclerView?.layoutManager = layoutManager
        }

        adapter = recyclerView?.adapter as? ItemRecyclerAdapter
        if (adapter == null) {
            adapter = ItemRecyclerAdapter(null, true)
            adapter?.context = this.activity
            adapter?.isHatching = this.isHatching
            adapter?.isFeeding = this.isFeeding
            adapter?.fragment = this
            if (this.hatchingItem != null) {
                adapter?.hatchingItem = this.hatchingItem
            }
            if (this.feedingPet != null) {
                adapter?.feedingPet = this.feedingPet
            }
            recyclerView?.adapter = adapter

            adapter?.let { adapter ->
                compositeSubscription.add(adapter.getSellItemFlowable()
                        .flatMap { item -> inventoryRepository.sellItem(user, item) }
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))

                compositeSubscription.add(adapter.getQuestInvitationFlowable()
                        .flatMap { quest -> inventoryRepository.inviteToQuest(quest) }
                        .subscribe(Consumer { MainNavigationController.navigate(R.id.partyFragment) }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.getOpenMysteryItemFlowable()
                        .flatMap { inventoryRepository.openMysteryItem(user) }
                        .flatMap { userRepository.retrieveUser(false) }
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.startHatchingEvents.subscribeWithErrorHandler(Consumer { showHatchingDialog(it) }))
                compositeSubscription.add(adapter.hatchPetEvents.subscribeWithErrorHandler(Consumer { hatchPet(it.first, it.second) }))
            }
        }
        activity?.let {
            recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(it, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
        }

        when {
            this.isHatching -> {
                dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                this.titleView?.text = getString(R.string.hatch_with, this.hatchingItem?.text)
                this.titleView?.visibility = View.VISIBLE
                this.footerView?.text = getString(R.string.hatching_market_info)
                this.footerView?.visibility = View.VISIBLE
                this.openMarketButton?.visibility = View.VISIBLE
            }
            this.isFeeding -> {
                dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                this.titleView?.text = getString(R.string.dialog_feeding, this.feedingPet?.text)
                this.titleView?.visibility = View.VISIBLE
                this.footerView?.text = getString(R.string.feeding_market_info)
                this.footerView?.visibility = View.VISIBLE
                this.openMarketButton?.visibility = View.VISIBLE
            }
            else -> {
                this.titleView?.visibility = View.GONE
                this.footerView?.visibility = View.GONE
                this.openMarketButton?.visibility = View.GONE
            }
        }

        openMarketButton?.setOnClickListener {
            dismiss()
            openMarket()
        }

        openEmptyMarketButton?.setOnClickListener { openMarket() }

        this.loadItems()
    }

    private fun showHatchingDialog(item: Item) {
        val fragment = ItemRecyclerFragment()
        if (item is Egg) {
            fragment.itemType = "hatchingPotions"
            fragment.hatchingItem = item
        } else {
            fragment.itemType = "eggs"
            fragment.hatchingItem = item
        }
        fragment.isHatching = true
        fragment.isFeeding = false
        fragment.show(fragmentManager, "hatchingDialog")
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
        compositeSubscription.add(this.inventoryRepository.hatchPet(egg, potion) {
            dismiss()
            val petWrapper = View.inflate(context, R.layout.pet_imageview, null) as? FrameLayout
            val petImageView = petWrapper?.findViewById(R.id.pet_imageview) as? SimpleDraweeView

            DataBindingUtils.loadImage(petImageView, "Pet-" + egg.key + "-" + potion.key)
            val potionName = potion.text
            val eggName = egg.text
            val dialog = context?.let { HabiticaAlertDialog(it) }
            dialog?.setTitle(getString(R.string.hatched_pet_title, potionName, eggName))
            dialog?.setAdditionalContentView(petWrapper)
            dialog?.addButton(R.string.onwards, true) { hatchingDialog, _ -> hatchingDialog.dismiss() }
            dialog?.addButton(R.string.share, false) { hatchingDialog, _ ->
                val event1 = ShareEvent()
                event1.sharedMessage = getString(R.string.share_hatched, potionName, eggName) + " https://habitica.com/social/hatch-pet"
                val petImageSideLength = 140
                val sharedImage = Bitmap.createBitmap(petImageSideLength, petImageSideLength, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(sharedImage)
                context?.let { canvas.drawColor(ContextCompat.getColor(it, R.color.brand_300)) }
                petImageView?.drawable?.setBounds(0, 0, petImageSideLength, petImageSideLength)
                petImageView?.drawable?.draw(canvas)
                event1.shareImage = sharedImage
                EventBus.getDefault().post(event1)
                hatchingDialog.dismiss()
            }
            dialog?.show()
        }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
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
                    .doOnNext { items ->
                        if (items.size > 0) {
                            adapter?.updateData(items)
                        }
                    }
                    .map { items -> items.mapNotNull { it.key } }
                    .flatMap { inventoryRepository.getItems(itemClass, it.toTypedArray(), user) }
                    .map {
                        val itemMap = mutableMapOf<String, Item>()
                        for (item in it) {
                            itemMap[item.key] = item
                        }
                        itemMap
                    }
                    .subscribe(Consumer { items ->
                    adapter?.items = items
            }, RxErrorHandler.handleEmptyError()))
        }

        compositeSubscription.add(inventoryRepository.getPets().subscribe(Consumer { adapter?.setExistingPets(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(inventoryRepository.getOwnedPets().firstElement()
                .map { ownedMounts ->
                    val mountMap = mutableMapOf<String, OwnedPet>()
                    ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                    return@map mountMap
                }
                .subscribe(Consumer { adapter?.setOwnedPets(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun openMarket() {
        MainNavigationController.navigate(R.id.shopsFragment)
    }

    companion object {

        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
