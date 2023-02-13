package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ShopHeaderBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ShopItemViewHolder
import com.habitrpg.common.habitica.extensions.fromHtml

class ShopRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var onNeedsRefresh: (() -> Unit)? = null

    private val items: MutableList<Any> = ArrayList()
    private var shopIdentifier: String? = null
    private var ownedItems: Map<String, OwnedItem> = HashMap()

    var changeClassEvents: ((String) -> Unit)? = null

    var shopSpriteSuffix: String = ""
        set(value) {
            field = value
            if (items.size > 0) {
                notifyItemChanged(0)
            }
        }
    var context: Context? = null
    var user: User? = null
        set(value) {
            field = value
            if (items.size > 0) {
                this.notifyDataSetChanged()
            }
        }
    private var pinnedItemKeys: List<String> = ArrayList()

    var gearCategories: MutableList<ShopCategory> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal var selectedGearCategory: String = ""
        set(value) {
            field = value
            if (field != "" && items.size > 0) {
                notifyDataSetChanged()
            }
        }

    private val emptyViewResource: Int
        get() = when (this.shopIdentifier) {
            Shop.SEASONAL_SHOP -> R.layout.empty_view_seasonal_shop
            Shop.TIME_TRAVELERS_SHOP -> R.layout.empty_view_timetravelers
            else -> R.layout.simple_textview
        }

    fun setShop(shop: Shop?) {
        if (shop == null) {
            return
        }
        shopIdentifier = shop.identifier
        items.clear()
        items.add(shop)
        for (category in shop.categories) {
            if (category.items.size > 0) {
                items.add(category)
                for (item in category.items) {
                    item.categoryIdentifier = category.identifier
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
        when (viewType) {
            0 -> ShopHeaderViewHolder(parent)
            1 -> SectionViewHolder(parent.inflate(R.layout.shop_section_header))
            2 -> EmptyStateViewHolder(parent.inflate(emptyViewResource))
            else -> {
                val view = parent.inflate(R.layout.row_shopitem)
                val viewHolder = ShopItemViewHolder(view)
                viewHolder.shopIdentifier = shopIdentifier
                viewHolder.onNeedsRefresh = onNeedsRefresh
                viewHolder
            }
        }

    @Suppress("ReturnCount")
    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        val obj = getItem(position)
        if (obj != null) {
            when (obj.javaClass) {
                Shop::class.java -> (obj as? Shop)?.let { (holder as? ShopHeaderViewHolder)?.bind(it, shopSpriteSuffix) }
                ShopCategory::class.java -> {
                    val category = obj as? ShopCategory
                    val sectionHolder = holder as? SectionViewHolder ?: return
                    sectionHolder.bind(category?.text ?: "")
                    if (gearCategories.contains(category)) {
                        context?.let { context ->
                            val adapter = HabiticaClassArrayAdapter(context, R.layout.class_spinner_dropdown_item, gearCategories.map { it.identifier })
                            sectionHolder.spinnerAdapter = adapter
                            sectionHolder.selectedItem = gearCategories.indexOf(category)
                            sectionHolder.spinnerSelectionChanged = {
                                if (selectedGearCategory != gearCategories[holder.selectedItem].identifier) {
                                    selectedGearCategory = gearCategories[holder.selectedItem].identifier
                                }
                            }
                            if (user?.stats?.habitClass != category?.identifier && category?.identifier != "none") {
                                sectionHolder.notesView?.text = context.getString(R.string.class_gear_disclaimer)
                                if (user?.hasClass == true) {
                                    sectionHolder.switchClassButton?.setOnClickListener {
                                        changeClassEvents?.invoke(selectedGearCategory)
                                    }
                                    // TODO: Enable this again when we have a nicer design
                                    sectionHolder.switchClassButton?.visibility = View.GONE
                                } else {
                                    sectionHolder.switchClassButton?.visibility = View.GONE
                                }
                                sectionHolder.notesWrapper?.visibility = View.VISIBLE
                            } else {
                                sectionHolder.notesWrapper?.visibility = View.GONE
                            }
                        }
                    } else {
                        sectionHolder.spinnerAdapter = null
                        sectionHolder.notesWrapper?.visibility = View.GONE
                    }
                }
                ShopItem::class.java -> {
                    val item = obj as? ShopItem ?: return
                    val itemHolder = holder as? ShopItemViewHolder ?: return
                    val numberOwned = ownedItems[item.key + "-" + item.purchaseType]?.numberOwned ?: 0
                    itemHolder.bind(item, item.canAfford(user, 1), numberOwned)
                    itemHolder.isPinned = pinnedItemKeys.contains(item.key)
                }
                String::class.java -> (holder as? EmptyStateViewHolder)?.text = obj as? String
            }
        }
    }

    @Suppress("ReturnCount")
    private fun getItem(position: Int): Any? {
        if (items.size == 0) {
            return null
        }
        if (position == 0) {
            return items[0]
        }
        if (position <= getGearItemCount()) {
            return when {
                position == 1 -> {
                    val category = getSelectedShopCategory()
                    category?.text = context?.getString(R.string.class_equipment) ?: ""
                    category
                }
                getSelectedShopCategory()?.items?.size ?: 0 <= position - 2 -> return context?.getString(R.string.equipment_empty)
                else -> getSelectedShopCategory()?.items?.get(position - 2)
            }
        } else {
            val itemPosition = position - getGearItemCount()
            if (itemPosition > items.size - 1) {
                return null
            }
            return items[itemPosition]
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)?.javaClass) {
        Shop::class.java -> 0
        ShopCategory::class.java -> 1
        ShopItem::class.java -> 3
        else -> 2
    }

    override fun getItemCount(): Int {
        val size = items.size + getGearItemCount()
        return if (size == 1) {
            2
        } else size
    }

    private fun getGearItemCount(): Int {
        return if (selectedGearCategory == "") {
            0
        } else {
            val selectedCategory: ShopCategory? = getSelectedShopCategory()
            if (selectedCategory != null) {
                if (selectedCategory.items.size == 0) {
                    2
                } else {
                    selectedCategory.items.size + 1
                }
            } else {
                0
            }
        }
    }

    private fun getSelectedShopCategory() =
        gearCategories.firstOrNull { selectedGearCategory == it.identifier }

    fun setOwnedItems(ownedItems: Map<String, OwnedItem>) {
        this.ownedItems = ownedItems
        if (items.size > 0) {
            this.notifyDataSetChanged()
        }
    }

    fun setPinnedItemKeys(pinnedItemKeys: List<String>) {
        this.pinnedItemKeys = pinnedItemKeys
        if (items.size > 0) {
            this.notifyDataSetChanged()
        }
    }

    internal class ShopHeaderViewHolder(parent: ViewGroup) : androidx.recyclerview.widget.RecyclerView.ViewHolder(parent.inflate(R.layout.shop_header)) {
        private val binding = ShopHeaderBinding.bind(itemView)

        init {
            binding.descriptionView.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(shop: Shop, shopSpriteSuffix: String) {
            binding.npcBannerView.shopSpriteSuffix = shopSpriteSuffix
            binding.npcBannerView.identifier = shop.identifier

            binding.descriptionView.text = shop.notes.fromHtml()
            binding.namePlate.setText(shop.npcNameResource)
        }
    }

    class EmptyStateViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val subscribeButton: Button? = itemView.findViewById(R.id.subscribeButton)
        private val textView: TextView? = itemView.findViewById(R.id.textView)

        init {
            subscribeButton?.setOnClickListener { MainNavigationController.navigate(R.id.gemPurchaseActivity) }
        }

        var text: String? = null
            set(value) {
                field = value
                textView?.text = field
            }
    }
}
