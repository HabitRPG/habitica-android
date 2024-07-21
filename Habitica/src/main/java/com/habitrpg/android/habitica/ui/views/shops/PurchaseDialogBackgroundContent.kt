package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.PurchaseDialogBackgroundBinding
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.common.habitica.extensionsCommon.layoutInflater
import com.habitrpg.common.habitica.viewsCommon.AvatarView
import com.habitrpg.common.habitica.viewsCommon.PixelArtView
import com.habitrpg.shared.habitica.models.Avatar
import java.util.EnumMap

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

    fun setAvatarWithBackgroundPreview(
        avatar: Avatar,
        item: ShopItem,
    ) {
        val layerMap = EnumMap<AvatarView.LayerType, String>(AvatarView.LayerType::class.java)
        layerMap[AvatarView.LayerType.BACKGROUND] = item.imageName?.removePrefix("icon_")

        binding.avatarView.setAvatar(avatar, layerMap)
    }
}
