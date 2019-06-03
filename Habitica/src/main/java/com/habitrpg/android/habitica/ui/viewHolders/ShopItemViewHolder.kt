package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.CurrencyView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog

class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val imageView: SimpleDraweeView by bindView(itemView, R.id.imageView)
    private val buyButton: View by bindView(itemView, R.id.buyButton)
    private val priceLabel: CurrencyView by bindView(itemView, R.id.priceLabel)
    private val unlockLabel: TextView by bindView(itemView, R.id.unlockLabel)
    private val itemDetailIndicator: TextView by bindView(itemView, R.id.item_detail_indicator)
    private val pinIndicator: ImageView by bindView(itemView, R.id.pin_indicator)

    var shopIdentifier: String? = null
    private var item: ShopItem? = null

    private var context: Context = itemView.context

    private var lockedDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorLocked())
    private var limitedDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorLimited())
    private var countDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfItemIndicatorNumber())

    var itemCount = 0
    set(value) {
        field = value
        if (value > 0) {
            itemDetailIndicator.text = value.toString()
            itemDetailIndicator.background = countDrawable
            itemDetailIndicator.visibility = View.VISIBLE
        }
    }

    var isPinned = false
    set(value) {
        field =value
        pinIndicator.visibility = if (isPinned) View.VISIBLE else View.GONE
    }

    init {
        itemView.setOnClickListener(this)
        itemView.isClickable = true
        pinIndicator.setImageBitmap(HabiticaIconsHelper.imageOfPinnedItem())
    }

    fun bind(item: ShopItem, canBuy: Boolean) {
        this.item = item
        buyButton.visibility = View.VISIBLE

        DataBindingUtils.loadImage(this.imageView, item.imageName)

        if (item.unlockCondition == null || !item.locked) {
            priceLabel.text = item.value.toString()
            priceLabel.currency = item.currency
            if (item.currency == null) {
                buyButton.visibility = View.GONE
            }
            priceLabel.visibility = View.VISIBLE
            unlockLabel.visibility = View.GONE
        } else {
            unlockLabel.setText(item.unlockCondition?.readableUnlockConditionId() ?: 0)
            priceLabel.visibility = View.GONE
            unlockLabel.visibility = View.VISIBLE
        }

        itemDetailIndicator.text = null
        itemDetailIndicator.visibility = View.GONE
        if (item.isLimited) {
            itemDetailIndicator.background = limitedDrawable
            itemDetailIndicator.visibility = View.VISIBLE
        }

        priceLabel.isLocked = item.locked || !canBuy
        if (item.locked) {
            itemDetailIndicator.background = lockedDrawable
            itemDetailIndicator.visibility = View.VISIBLE
        }
    }

    override fun onClick(view: View) {
        val item = item
        if (item != null) {
            val dialog = PurchaseDialog(context, HabiticaBaseApplication.userComponent, item)
            dialog.shopIdentifier = shopIdentifier
            dialog.isPinned = isPinned
            dialog.show()
        }
    }

    fun hidePinIndicator() {
        pinIndicator.visibility = View.GONE
    }
}
