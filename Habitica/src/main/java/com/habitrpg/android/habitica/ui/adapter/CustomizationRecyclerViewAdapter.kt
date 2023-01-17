package com.habitrpg.android.habitica.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CustomizationGridItemBinding
import com.habitrpg.android.habitica.databinding.CustomizationSectionFooterBinding
import com.habitrpg.android.habitica.databinding.CustomizationSectionHeaderBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.AvatarView
import com.habitrpg.shared.habitica.models.Avatar
import java.util.Date
import java.util.EnumMap
import kotlin.math.min

class CustomizationRecyclerViewAdapter() : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var userSize: String? = null
    var hairColor: String? = null
    var avatar: Avatar? = null
    var customizationType: String? = null
    var gemBalance = 0
    var columnCount = 1
    var unsortedCustomizations: List<Customization> = ArrayList()
    private var customizationList: MutableList<Any> = ArrayList()
    var additionalSetItems: List<Customization> = ArrayList()
    var activeCustomization: String? = null
        set(value) {
            field = value
        }

    var ownedCustomizations: List<String> = listOf()
    private var pinnedItemKeys: List<String> = ArrayList()

    var onCustomizationSelected: ((Customization) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.customization_section_header, parent, false)
                SectionViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.customization_section_footer, parent, false)
                SectionFooterViewHolder(view)
            }
            else -> {
                val viewID: Int = if (customizationType == "background") {
                    R.layout.customization_grid_background_item
                } else {
                    R.layout.customization_grid_item
                }

                val view = LayoutInflater.from(parent.context).inflate(viewID, parent, false)
                CustomizationViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        val obj = customizationList[position]
        if (getItemViewType(position) == 0) {
            (holder as SectionViewHolder).bind(obj as CustomizationSet)
        } else if (getItemViewType(position) == 1) {
            (holder as SectionFooterViewHolder).bind(obj as CustomizationSet)
            val count = min(columnCount, obj.customizations.size)
            holder.buttonWidth = (count * 76.dpToPx(holder.itemView.context)) + ((count - 1) * 12.dpToPx(holder.itemView.context))
            holder.additionalSetItems = additionalSetItems.filter { it.purchasable && (it.price ?: 0) > 0 }
        } else {
            (holder as CustomizationViewHolder).bind(customizationList[position] as Customization)
        }
    }

    override fun getItemCount(): Int {
        return customizationList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (customizationList.size <= position) return 0
        return if (this.customizationList[position] is CustomizationSet &&
            (position == (customizationList.size - 1) || (position < customizationList.size && customizationList[position + 1] is CustomizationSet))
        ) {
            1
        } else if (this.customizationList[position] is CustomizationSet) {
            0
        } else {
            2
        }
    }

    fun setCustomizations(newCustomizationList: List<Customization>) {
        unsortedCustomizations = newCustomizationList
        this.customizationList = ArrayList()
        var lastSet = CustomizationSet()
        val today = Date()
        for (customization in newCustomizationList) {
            val isUsable = customization.isUsable(ownedCustomizations.contains(customization.id))
            if (customization.availableFrom != null || customization.availableUntil != null) {
                if (((customization.availableFrom?.compareTo(today)
                        ?: 0) > 0 || (customization.availableUntil?.compareTo(today)
                        ?: 0) < 0) && !isUsable
                ) {
                    continue
                }
            }
            if ((customization.identifier?.contains("HabitversaryBash") == true || customization.identifier?.contains("birthday") == true) && !isUsable) {
                continue
            }
            if (customization.customizationSet != null && customization.customizationSet != lastSet.identifier) {
                if (lastSet.hasPurchasable && lastSet.price > 0) {
                    customizationList.add(lastSet)
                }
                val set = CustomizationSet()
                set.identifier = customization.customizationSet
                set.text = customization.customizationSetName
                set.price = customization.setPrice ?: 0
                set.hasPurchasable = true
                lastSet = set
                customizationList.add(set)
            }
            customizationList.add(customization)
            lastSet.customizations.add(customization)
            if (isUsable && lastSet.hasPurchasable) {
                lastSet.ownedCustomizations.add(customization)
                if (!lastSet.isSetDeal()) {
                    lastSet.hasPurchasable = false
                }
            }
        }
        if (lastSet.hasPurchasable) {
            customizationList.add(lastSet)
        }
        this.notifyDataSetChanged()
    }

    fun setPinnedItemKeys(pinnedItemKeys: List<String>) {
        this.pinnedItemKeys = pinnedItemKeys
        if (customizationList.size > 0) this.notifyDataSetChanged()
    }

    internal inner class CustomizationViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = CustomizationGridItemBinding.bind(itemView)
        var customization: Customization? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(customization: Customization) {
            this.customization = customization

            if (customization.type == "background" && customization.identifier == "") {
                binding.imageView.setImageResource(R.drawable.no_background)
                binding.imageView.bitmap = null
            } else {
                binding.imageView.loadImage(customization.getIconName(userSize, hairColor))
            }

            if (customization.isUsable(ownedCustomizations.contains(customization.id))) {
                binding.buyButton.visibility = View.GONE
            } else {
                binding.buyButton.visibility = View.VISIBLE
                if (customization.customizationSet?.contains("timeTravel") == true) {
                    binding.priceLabel.currency = "hourglasses"
                } else {
                    binding.priceLabel.currency = "gems"
                }
                binding.priceLabel.value = customization.price?.toDouble() ?: 0.0
            }
            val identifier = if (customization.type == "chair") "chair_${customization.identifier}" else customization.identifier
            if (activeCustomization == identifier) {
                binding.wrapper.background = ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window_tint_border)
            } else {
                binding.wrapper.background = ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window)
            }
        }

        override fun onClick(v: View) {
            if (customization?.isUsable(ownedCustomizations.contains(customization?.id)) == false) {
                if (customization?.customizationSet?.contains("timeTravel") == true) {
                    val dialog = HabiticaAlertDialog(itemView.context)
                    dialog.setMessage(R.string.purchase_from_timetravel_shop)
                    dialog.addButton(R.string.go_shopping, true) { _, _ ->
                        MainNavigationController.navigate(R.id.timeTravelersShopFragment)
                    }
                    dialog.addButton(R.string.reward_dialog_dismiss, false)
                    dialog.show()
                } else {
                    customization?.let {
                        val dialog = PurchaseDialog(itemView.context, HabiticaBaseApplication.userComponent, ShopItem.fromCustomization(it, userSize, hairColor))
                        if (it.type == "background") dialog.isPinned = pinnedItemKeys.contains(ShopItem.fromCustomization(it, userSize, hairColor).key)
                        dialog.show()
                    }
                }
                return
            }

            if (customization?.type != "background" && customization?.identifier == activeCustomization) {
                return
            }

            if (customization?.type == "background"){
                val alert = HabiticaAlertDialog(context = itemView.context)
                val purchasedCustomizationView: View = LayoutInflater.from(itemView.context).inflate(R.layout.purchased_equip_dialog, null)
                val layerMap = EnumMap<AvatarView.LayerType, String>(AvatarView.LayerType::class.java)
                avatar?.let { avatar ->
                    layerMap[AvatarView.LayerType.BACKGROUND] = customization?.let { ShopItem.fromCustomization(it, userSize, hairColor).imageName }
                    purchasedCustomizationView.findViewById<AvatarView>(R.id.avatar_view).setAvatar(avatar, layerMap)
                }
                alert.setAdditionalContentView(purchasedCustomizationView)
                alert.setTitle(customization?.text)
                alert.setMessage(customization?.notes)
                alert.addButton(R.string.equip, true) { _, _ ->
                    customization?.let {
                        onCustomizationSelected?.invoke(it)
                    }
                }
                alert.addButton(R.string.close, false) { _, _ ->
                    alert.dismiss()
                }
                alert.show()
            } else {
                customization?.let {
                    onCustomizationSelected?.invoke(it)
                }
            }
        }
    }

    internal inner class SectionViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private val binding = CustomizationSectionHeaderBinding.bind(itemView)

        fun bind(set: CustomizationSet) {
            binding.label.text = set.text
        }
    }

    internal inner class SectionFooterViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = CustomizationSectionFooterBinding.bind(itemView)
        var context: Context = itemView.context
        private var set: CustomizationSet? = null
        var additionalSetItems: List<Customization>? = null

        var buttonWidth: Int
        get() = binding.purchaseSetButton.width
        set(value) {
            val params = binding.purchaseSetButton.layoutParams
            params.width = value
            binding.purchaseSetButton.layoutParams = params
        }

        init {
            binding.purchaseSetButton.setOnClickListener(this)
        }

        fun bind(set: CustomizationSet) {
            this.set = set
            if (set.hasPurchasable && set.identifier?.contains("timeTravel") != true) {
                binding.purchaseSetButton.visibility = View.VISIBLE
                binding.setPriceLabel.value = set.price.toDouble()
                binding.setPriceLabel.currency = "gems"
            } else {
                binding.purchaseSetButton.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            set?.let {
                val dialog = PurchaseDialog(itemView.context, HabiticaBaseApplication.userComponent, ShopItem.fromCustomizationSet(it, additionalSetItems, userSize, hairColor))
                dialog.show()
            }
        }
    }
}
