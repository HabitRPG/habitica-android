package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.DialogPurchaseContentItemBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.shops.ShopItem

class PurchaseDialogItemContent(context: Context) : PurchaseDialogContent(context) {
    private val binding = DialogPurchaseContentItemBinding.inflate(context.layoutInflater, this)
    override val imageView: ImageView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setItem(item: ShopItem) {
        super.setItem(item)
        binding.notesTextView.text = item.notes
        binding.stepperView.iconDrawable = null
    }
}
