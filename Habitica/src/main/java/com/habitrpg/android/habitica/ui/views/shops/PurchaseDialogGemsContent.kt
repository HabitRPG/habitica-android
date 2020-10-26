package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.databinding.DialogPurchaseGemsBinding
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

internal class PurchaseDialogGemsContent(context: Context) : PurchaseDialogContent(context) {
    internal val binding = DialogPurchaseGemsBinding.inflate(context.layoutInflater, this)
    override val imageView: SimpleDraweeView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView

    init {
        binding.stepperView.iconDrawable = HabiticaIconsHelper.imageOfGem().asDrawable(context.resources)
    }

    override fun setItem(item: ShopItem) {
        super.setItem(item)
        binding.notesTextView.text = item.notes
    }
}
