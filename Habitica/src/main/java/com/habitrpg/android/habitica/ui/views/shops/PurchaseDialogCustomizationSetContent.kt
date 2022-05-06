package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogPurchaseCustomizationsetBinding
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.helpers.loadImage

class PurchaseDialogCustomizationSetContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseCustomizationsetBinding.inflate(context.layoutInflater, this)
    override val imageView: ImageView
        get() = ImageView(context)
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setItem(item: ShopItem) {
        titleTextView.text = item.text
        binding.imageViewWrapper.removeAllViews()
        item.setImageNames.forEach {
            val imageView = ImageView(context)
            imageView.setBackgroundResource(R.drawable.layout_rounded_bg_window)
            imageView.loadImage(it)
            imageView.layoutParams = FlexboxLayout.LayoutParams(76.dpToPx(context), 76.dpToPx(context))
            binding.imageViewWrapper.addView(imageView)
        }
    }
}