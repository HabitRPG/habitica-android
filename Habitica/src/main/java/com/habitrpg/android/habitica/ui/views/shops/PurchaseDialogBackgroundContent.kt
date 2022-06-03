package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.PurchaseDialogBackgroundBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.views.PixelArtView
import java.util.*

class PurchaseDialogBackgroundContent(context: Context) : PurchaseDialogContent(context) {
    val binding = PurchaseDialogBackgroundBinding.inflate(context.layoutInflater, this)
    override val imageView: PixelArtView
        get() = PixelArtView(context)
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setItem(item: ShopItem) {
        binding.titleTextView.text = item.text
        binding.notesTextView.text = item.notes
    }

    fun setAvatarWithBackgroundPreview(avatar: Avatar, item: ShopItem) {
        val layerMap = EnumMap<AvatarView.LayerType, String>(AvatarView.LayerType::class.java)
        layerMap[AvatarView.LayerType.BACKGROUND] = item.imageName

        binding.avatarView.setAvatar(avatar, layerMap)
    }
}
