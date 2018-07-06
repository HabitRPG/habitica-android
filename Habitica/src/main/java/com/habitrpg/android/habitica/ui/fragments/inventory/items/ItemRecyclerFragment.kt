package com.habitrpg.android.habitica.ui.fragments.inventory.items

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.inventory.ItemRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.helpers.RecyclerViewEmptySupport
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import io.realm.OrderedRealmCollection
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class ItemRecyclerFragment : BaseFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository
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
    internal var layoutManager: LinearLayoutManager? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_items)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        recyclerView?.setEmptyView(emptyView)
        emptyTextView?.text = getString(R.string.empty_items, itemTypeText)

        val context = activity

        layoutManager = recyclerView?.layoutManager as LinearLayoutManager?

        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(context)

            recyclerView?.layoutManager = layoutManager
        }

        adapter = recyclerView?.adapter as ItemRecyclerAdapter?
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

            adapter?.notNull {
                compositeSubscription.add(it.sellItemEvents
                        .flatMap { item -> inventoryRepository.sellItem(user, item) }
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))

                compositeSubscription.add(it.questInvitationEvents
                        .flatMap { quest -> inventoryRepository.inviteToQuest(quest) }
                        .subscribe(Consumer { EventBus.getDefault().post(OpenMenuItemCommand(NavigationDrawerFragment.SIDEBAR_PARTY)) }, RxErrorHandler.handleEmptyError()))
            }
        }
        activity.notNull {
            recyclerView?.addItemDecoration(DividerItemDecoration(it, DividerItemDecoration.VERTICAL))
        }
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
        }

        if (this.isHatching) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.titleView?.text = getString(R.string.hatch_with, this.hatchingItem?.text)
            this.titleView?.visibility = View.VISIBLE
            this.footerView?.text = getString(R.string.hatching_market_info)
            this.footerView?.visibility = View.VISIBLE
            this.openMarketButton?.visibility = View.VISIBLE
        } else if (this.isFeeding) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.titleView?.text = getString(R.string.dialog_feeding, this.feedingPet?.colorText, this.feedingPet?.animalText)
            this.titleView?.visibility = View.VISIBLE
            this.footerView?.text = getString(R.string.feeding_market_info)
            this.footerView?.visibility = View.VISIBLE
            this.openMarketButton?.visibility = View.VISIBLE
        } else {
            this.titleView?.visibility = View.GONE
            this.footerView?.visibility = View.GONE
            this.openMarketButton?.visibility = View.GONE
        }

        openMarketButton?.setOnClickListener {
            dismiss()
            openMarket()
        }

        openEmptyMarketButton?.setOnClickListener { openMarket() }

        this.loadItems()
    }

    override fun onResume() {
        if ((this.isHatching || this.isFeeding) && dialog.window != null) {
            val params = dialog.window?.attributes
            params?.width = ViewGroup.LayoutParams.MATCH_PARENT
            params?.verticalMargin = 60f
            dialog.window?.attributes = params
        }

        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ITEM_TYPE_KEY, this.itemType)
    }

    private fun loadItems() {
        var itemClass: Class<out Item>? = null
        when (itemType) {
            "eggs" -> itemClass = Egg::class.java
            "hatchingPotions" -> itemClass = HatchingPotion::class.java
            "food" -> itemClass = Food::class.java
            "quests" -> itemClass = QuestContent::class.java
            "special" -> itemClass = SpecialItem::class.java
        }
        inventoryRepository.getOwnedItems(itemClass, user).firstElement().subscribe(Consumer { items ->
            if (items.size > 0) {
                adapter?.updateData(items as OrderedRealmCollection<Item>)
            }
        }, RxErrorHandler.handleEmptyError())

        compositeSubscription.add(inventoryRepository?.ownedPets.subscribe(Consumer { adapter?.setOwnedPets(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun openMarket() {
        EventBus.getDefault().post(OpenMenuItemCommand(NavigationDrawerFragment.SIDEBAR_SHOPS))
    }

    companion object {

        private val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
