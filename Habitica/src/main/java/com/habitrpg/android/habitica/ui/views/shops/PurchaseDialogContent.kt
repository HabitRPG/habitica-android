package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

abstract class PurchaseDialogContent @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    abstract val imageView: SimpleDraweeView
    abstract val titleTextView: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }

    open fun setItem(item: ShopItem) {
        if (item.path?.contains("timeTravelBackgrounds") == true) {
            val controller = Fresco.newDraweeControllerBuilder()
                    .setUri("https://habitica-assets.s3.amazonaws.com/mobileApp/images/${item.imageName?.replace("icon_", "")}.gif")
                    .setAutoPlayAnimations(true)
                    .build()
            imageView.controller = controller
            val params = imageView.layoutParams
            params.height = 147.dpToPx(context)
            params.width = 140.dpToPx(context)
            imageView.layoutParams = params
        } else {
            DataBindingUtils.loadImage(imageView, item.imageName)
        }
        titleTextView.text = item.text
    }

    open fun setQuestContentItem(questContent: QuestContent) {
        DataBindingUtils.loadImage(imageView, "inventory_quest_scroll_" + questContent.key)
        titleTextView.setText(questContent.text.fromHtml(), TextView.BufferType.SPANNABLE)
    }
}
