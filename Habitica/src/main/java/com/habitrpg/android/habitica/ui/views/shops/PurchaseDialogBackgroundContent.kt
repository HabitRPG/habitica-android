package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.PurchaseDialogBackgroundBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.shops.ShopItem

class PurchaseDialogBackgroundContent(context: Context) : PurchaseDialogContent(context) {
    val binding = PurchaseDialogBackgroundBinding.inflate(context.layoutInflater, this)
    override val imageView: ImageView
        get() = ImageView(context)
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setItem(item: ShopItem) {
        binding.titleTextView.text = item.text
        binding.notesTextView.text = item.notes
    }

    fun setAvatar(avatar: Avatar) {
        binding.avatarView.setAvatar(avatar)
    }
}