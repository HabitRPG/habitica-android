package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogPurchaseCustomizationBinding
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.views.AvatarView
import com.habitrpg.common.habitica.views.PixelArtView
import java.util.EnumMap

class PurchaseDialogCustomizationContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseCustomizationBinding.inflate(context.layoutInflater, this)
    override val imageView: PixelArtView
        get() = PixelArtView(context)
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setItem(item: ShopItem) {
        super.setItem(item)
        if (item.text?.isNotBlank() != true) {
            titleTextView.text = buildCustomizationTitle(item)
        }
    }

    private fun buildCustomizationTitle(item: ShopItem): CharSequence? {
        val path = item.unlockPath ?: item.path ?: return null
        return when {
            path.contains("skin") -> context.getString(R.string.avatar_skin_customization)
            path.contains("shirt") -> context.getString(R.string.avatar_shirt_customization)
            path.contains("color") -> context.getString(R.string.avatar_hair_color_customization)
            path.contains("base") -> context.getString(R.string.avatar_hair_style_customization)
            path.contains("bangs") -> context.getString(R.string.avatar_bangs_customization)
            path.contains("beard") -> context.getString(R.string.avatar_beard_customization)
            path.contains("mustache") -> context.getString(R.string.avatar_mustache_customization)
            else -> null
        }
    }

    fun setAvatarWithPreview(
        user: User,
        shopItem: ShopItem,
    ) {
        val layerMap = EnumMap<AvatarView.LayerType, String>(AvatarView.LayerType::class.java)
        val path = shopItem.unlockPath ?: shopItem.path ?: ""
        val layerName =
            when {
                path.contains("skin") -> AvatarView.LayerType.SKIN
                path.contains("shirt") -> AvatarView.LayerType.SHIRT
                path.contains("color") -> AvatarView.LayerType.HAIR_BANGS
                path.contains("base") -> AvatarView.LayerType.HAIR_BASE
                path.contains("bangs") -> AvatarView.LayerType.HAIR_BANGS
                path.contains("beard") -> AvatarView.LayerType.HAIR_BEARD
                path.contains("mustache") -> AvatarView.LayerType.HAIR_MUSTACHE
                else -> null
            }
        layerName?.let {
            layerMap[it] = shopItem.imageName?.replace("shop_", "")?.replace("icon_", "")
            if (path.contains("color")) {
                val hairColor = shopItem.key.split("_").last()
                if ((user.preferences?.hair?.bangs ?: 0) > 0) {
                    layerMap[AvatarView.LayerType.HAIR_BANGS] = "hair_bangs_" + user.preferences?.hair?.bangs + "_" + hairColor
                }
                if ((user.preferences?.hair?.base ?: 0) > 0) {
                    layerMap[AvatarView.LayerType.HAIR_BASE] = "hair_base_" + user.preferences?.hair?.base + "_" + hairColor
                }
                if ((user.preferences?.hair?.mustache ?: 0) > 0) {
                    layerMap[AvatarView.LayerType.HAIR_MUSTACHE] = "hair_mustache_" + user.preferences?.hair?.mustache + "_" + hairColor
                }
                if ((user.preferences?.hair?.beard ?: 0) > 0) {
                    layerMap[AvatarView.LayerType.HAIR_BEARD] = "hair_beard_" + user.preferences?.hair?.beard + "_" + hairColor
                }
            }

            if (path.contains("hair")) {
                layerMap[AvatarView.LayerType.HEAD] = ""
                layerMap[AvatarView.LayerType.HEAD_ACCESSORY] = ""
            }

            if (path.contains("shirt")) {
                layerMap[AvatarView.LayerType.ARMOR] = ""
                layerMap[AvatarView.LayerType.BODY] = ""
            }
        }
        binding.avatarView.setAvatar(user, layerMap)
    }
}
