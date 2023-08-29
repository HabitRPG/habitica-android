package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RowShopitemBinding
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.loadImage

class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private val binding = RowShopitemBinding.bind(itemView)
    var shopIdentifier: String? = null
    private var item: ShopItem? = null
    var onNeedsRefresh: (() -> Unit)? = null
    var onShowPurchaseDialog: ((ShopItem, Boolean) -> Unit)? = null

    private var context: Context = itemView.context

    var purchaseCardAction: ((ShopItem) -> Unit)? = null

    var isPinned = false
        set(value) {
            field = value
            binding.pinIndicator.visibility = if (field) View.VISIBLE else View.GONE
        }

    var isCompleted = false
        set(value) {
            field = value
            binding.completedIndicator.visibility = if (field) View.VISIBLE else View.GONE
        }

    init {
        itemView.setOnClickListener(this)
        itemView.isClickable = true
        binding.pinIndicator.setImageBitmap(HabiticaIconsHelper.imageOfPinnedItem())
    }

    fun bind(item: ShopItem, canBuy: Boolean, numberOwned: Int) {
        this.item = item
        binding.buyButton.visibility = View.VISIBLE

        binding.imageView.loadImage(item.imageName?.replace("_locked", ""))

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
        val isLimited = item.isLimited || item.event?.end != null
        if (numberOwned > 0) {
            binding.itemDetailIndicator.text = numberOwned.toString()
            binding.itemDetailIndicator.background =
                AppCompatResources.getDrawable(context, R.drawable.pill_bg_gray)
            binding.itemDetailIndicator.visibility = View.VISIBLE
        }
        if (item.locked) {
            binding.itemDetailIndicator.background = AppCompatResources.getDrawable(context, R.drawable.shop_locked)
            binding.itemDetailIndicator.visibility = View.VISIBLE
        } else if (isLimited) {
            if (numberOwned == 0) {
                binding.itemDetailIndicator.background = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorLimited())
            } else {
                binding.itemDetailIndicator.background = AppCompatResources.getDrawable(context, R.drawable.pill_bg_purple_300)
            }
            binding.itemDetailIndicator.visibility = View.VISIBLE
        }

        if (item.key == "gem" && item.limitedNumberLeft == -1) {
            binding.itemDetailIndicator.background = AppCompatResources.getDrawable(context, R.drawable.item_indicator_subscribe)
            binding.itemDetailIndicator.visibility = View.VISIBLE
        }

        if (binding.itemDetailIndicator.visibility == View.VISIBLE) {
            val layoutParams = binding.itemDetailIndicator.layoutParams
            layoutParams.width = if (binding.itemDetailIndicator.text.isBlank()) {
                24.dpToPx(context)
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
            binding.itemDetailIndicator.layoutParams = layoutParams
        }

        binding.priceLabel.isLocked = item.locked || !canBuy
    }

    override fun onClick(view: View) {
        val item = item
        if (item != null && item.isValid) {
            onShowPurchaseDialog?.invoke(item, isPinned)
        }
    }

    fun hidePinIndicator() {
        binding.pinIndicator.visibility = View.GONE
    }
}
