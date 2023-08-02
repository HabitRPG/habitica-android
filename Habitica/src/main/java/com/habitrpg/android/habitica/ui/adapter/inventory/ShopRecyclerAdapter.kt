package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ShopArmoireGearBinding
import com.habitrpg.android.habitica.databinding.ShopHeaderBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ShopItemViewHolder
import com.habitrpg.android.habitica.ui.views.getTranslatedClassName
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.loadImage

class ShopRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    var armoireCount: Int = 0
    var onNeedsRefresh: (() -> Unit)? = null
    var onShowPurchaseDialog: ((ShopItem, Boolean) -> Unit)? = null

    private val items: MutableList<Any> = ArrayList()
    private var shopIdentifier: String? = null
    private var ownedItems: Map<String, OwnedItem> = HashMap()
    var armoireItem: ShopItem? = null

    var changeClassEvents: ((String) -> Unit)? = null

    var shopSpriteSuffix: String = ""
        set(value) {
            field = value
            if (items.size > 0) {
                notifyItemChanged(0)
            }
        }
    var context: Context? = null
    var mainActivity: MainActivity? = null
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            0 -> ShopHeaderViewHolder(parent)
            1 -> SectionViewHolder(parent.inflate(R.layout.shop_section_header))
            2 -> EmptyStateViewHolder(parent.inflate(emptyViewResource))
            3 -> {
                val viewHolder = ArmoireGearViewHolder(parent.inflate(R.layout.shop_armoire_gear))
                viewHolder.itemView.setOnClickListener {
                    armoireItem?.let { it1 -> onShowPurchaseDialog?.invoke(it1, true) }
                }
                viewHolder
            }
            else -> {
                val view = parent.inflate(R.layout.row_shopitem)
                val viewHolder = ShopItemViewHolder(view)
                viewHolder.shopIdentifier = shopIdentifier
                viewHolder.onNeedsRefresh = onNeedsRefresh
                viewHolder.onShowPurchaseDialog = onShowPurchaseDialog
                viewHolder
            }
        }

    @Suppress("ReturnCount")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val obj = getItem(position)
        if (obj != null) {
            when (obj) {
                is Shop -> (holder as? ShopHeaderViewHolder)?.bind(obj, shopSpriteSuffix)
                is ShopCategory -> {
                    val sectionHolder = holder as? SectionViewHolder ?: return
                    sectionHolder.bind(obj.text)
                    if (gearCategories.contains(obj)) {
                        context?.let { context ->
                            val adapter = HabiticaClassArrayAdapter(context, R.layout.class_spinner_dropdown_item, gearCategories.map { it.identifier })
                            sectionHolder.spinnerAdapter = adapter
                            sectionHolder.selectedItem = gearCategories.indexOf(obj)
                            sectionHolder.spinnerSelectionChanged = {
                                if (selectedGearCategory != gearCategories[holder.selectedItem].identifier) {
                                    selectedGearCategory = gearCategories[holder.selectedItem].identifier
                                }
                            }
                            if (user?.stats?.habitClass != obj.identifier && obj.identifier != "none") {
                                if (user?.hasClass == true) {
                                    sectionHolder.switchClassButton?.setOnClickListener {
                                        if ((user?.gemCount ?: 0) >= 3) {
                                            changeClassEvents?.invoke(selectedGearCategory)
                                        } else {
                                            mainActivity?.let { activity ->
                                                val dialog = InsufficientGemsDialog(activity, 3)
                                                Analytics.sendEvent("show insufficient gems modal", EventCategory.BEHAVIOUR, HitType.EVENT, mapOf("reason" to "class change"))
                                                dialog.show()
                                            }
                                        }
                                    }
                                    sectionHolder.switchClassButton?.visibility = View.VISIBLE
                                    sectionHolder.switchClassLabel?.text = context.getString(R.string.change_class_to_x, getTranslatedClassName(context.resources, selectedGearCategory))
                                    sectionHolder.switchClassDescription?.text = context.getString(R.string.unlock_gear_and_skills, getTranslatedClassName(context.resources, selectedGearCategory))
                                    sectionHolder.switchClassCurrency?.value = 3.0
                                } else {
                                    sectionHolder.switchClassButton?.visibility = View.GONE
                                }
                            } else {
                                sectionHolder.switchClassButton?.visibility = View.GONE
                            }
                        }
                    } else {
                        sectionHolder.spinnerAdapter = null
                        sectionHolder.notesView?.visibility = View.GONE
                    }
                }
                is ShopItem -> {
                    val itemHolder = holder as? ShopItemViewHolder ?: return
                    val numberOwned = ownedItems[obj.key + "-" + obj.purchaseType]?.numberOwned ?: 0
                    itemHolder.bind(obj, obj.canAfford(user, 1), numberOwned)
                    itemHolder.isPinned = pinnedItemKeys.contains(obj.key)
                }
                is String -> (holder as? EmptyStateViewHolder)?.text = obj
                is Pair<*, *> -> (holder as? ArmoireGearViewHolder)?.bind(obj.first as? String ?: "", obj.second as? Int ?: 0)
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
            val selectedGearCategory = getSelectedShopCategory()
            return when {
                position == 1 -> {
                    selectedGearCategory?.text = context?.getString(R.string.class_equipment) ?: ""
                    selectedGearCategory
                }
                (selectedGearCategory?.items?.size ?: 0) <= position - 2 -> return Pair(
                    context?.resources?.let { getTranslatedClassName(it, selectedGearCategory?.identifier) } ?: selectedGearCategory?.identifier,
                    armoireCount
                )
                else -> selectedGearCategory?.items?.get(position - 2)
            }
        } else {
            val itemPosition = position - getGearItemCount()
            if (itemPosition > items.size - 1) {
                return null
            }
            return items[itemPosition]
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Shop -> 0
        is ShopCategory -> 1
        is Pair<*, *> -> 3
        is ShopItem -> 4
        else -> 2
    }

    override fun getItemCount(): Int {
        val size = items.size + getGearItemCount()
        return if (size == 1) {
            2
        } else {
            size
        }
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

    internal class ShopHeaderViewHolder(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.shop_header)) {
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

    class EmptyStateViewHolder(view: View) : ViewHolder(view) {
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

    class ArmoireGearViewHolder(view: View) : ViewHolder(view) {
        private val binding = ShopArmoireGearBinding.bind(view)

        init {
            binding.currencyView.value = 100.0
            binding.iconView.loadImage("shop_armoire")
        }

        fun bind(className: String, armoireCount: Int) {
            binding.titleView.text = itemView.context.getString(R.string.shop_armoire_title, className)
            if (armoireCount > 0) {
                binding.descriptionView.text = itemView.context.getString(R.string.shop_armoire_description, armoireCount)
                binding.footerLayout.visibility = View.VISIBLE
                binding.iconView.visibility = View.VISIBLE
            } else {
                binding.descriptionView.text = itemView.context.getString(R.string.shop_armoire_empty_description)
                binding.footerLayout.visibility = View.INVISIBLE
                binding.iconView.visibility = View.GONE
            }
        }
    }
}
