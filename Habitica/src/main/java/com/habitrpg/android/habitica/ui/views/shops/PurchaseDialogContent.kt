package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

abstract class PurchaseDialogContent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    abstract val imageView: PixelArtView
    abstract val titleTextView: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }

    open fun setItem(item: ShopItem) {
        if (item.path?.contains("timeTravelBackgrounds") == true) {
            imageView.loadImage(item.imageName?.replace("icon_", ""))
            val params = imageView.layoutParams
            params.height = 147.dpToPx(context)
            params.width = 140.dpToPx(context)
            imageView.layoutParams = params
        } else {
            imageView.loadImage(item.imageName)
        }
        titleTextView.text = item.text
    }

    open fun setQuestContentItem(questContent: QuestContent) {
        imageView.loadImage("inventory_quest_scroll_" + questContent.key)
        titleTextView.setText(questContent.text.fromHtml(), TextView.BufferType.SPANNABLE)
    }
}
