package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.databinding.RowShopitemBinding
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog

class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private val binding = RowShopitemBinding.bind(itemView)
    var shopIdentifier: String? = null
    private var item: ShopItem? = null

    private var context: Context = itemView.context

    var purchaseCardAction: ((ShopItem) -> Unit)? = null

    var itemCount = 0
    set(value) {
        field = value
        if (value > 0) {
            binding.itemDetailIndicator.text = value.toString()
            binding.itemDetailIndicator.background = if (context.isUsingNightModeResources()) {
                BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorNumberDark())
            } else {
                BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorNumber())
            }
            binding.itemDetailIndicator.visibility = View.VISIBLE
        }
    }

    var isPinned = false
    set(value) {
        field =value
        binding.pinIndicator.visibility = if (isPinned) View.VISIBLE else View.GONE
    }

    init {
        itemView.setOnClickListener(this)
        itemView.isClickable = true
        binding.pinIndicator.setImageBitmap(HabiticaIconsHelper.imageOfPinnedItem())
    }

    fun bind(item: ShopItem, canBuy: Boolean) {
        this.item = item
        binding.buyButton.visibility = View.VISIBLE

        DataBindingUtils.loadImage(binding.imageView, item.imageName?.replace("_locked", ""))

        binding.itemDetailIndicator.text = null
        binding.itemDetailIndicator.visibility = View.GONE

        val lockedReason = item.shortLockedReason(context)
        if (!item.locked || lockedReason == null) {
            binding.priceLabel.text = item.value.toString()
            binding.priceLabel.currency = item.currency
            if (item.currency == null) {
                binding.buyButton.visibility = View.GONE
            }
            binding.priceLabel.visibility = View.VISIBLE
            binding.unlockLabel.visibility = View.GONE
        } else {
            binding.unlockLabel.text = lockedReason
            binding.priceLabel.visibility = View.GONE
            binding.unlockLabel.visibility = View.VISIBLE
        }
        if (item.locked) {
            binding.itemDetailIndicator.background = if (context.isUsingNightModeResources()) {
                BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorLockedDark())
            } else {
                BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorLocked())
            }
            binding.itemDetailIndicator.visibility = View.VISIBLE
        } else if (item.isLimited) {
            binding.itemDetailIndicator.background = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorLimited())
            binding.itemDetailIndicator.visibility = View.VISIBLE
        }

        binding.priceLabel.isLocked = item.locked || !canBuy
    }

    override fun onClick(view: View) {
        val item = item
        if (item != null && item.isValid) {
            val dialog = PurchaseDialog(context, HabiticaBaseApplication.userComponent, item)
            dialog.shopIdentifier = shopIdentifier
            dialog.isPinned = isPinned
            dialog.purchaseCardAction = {
                purchaseCardAction?.invoke(it)
            }
            dialog.show()
        }
    }

    fun hidePinIndicator() {
        binding.pinIndicator.visibility = View.GONE
    }
}
