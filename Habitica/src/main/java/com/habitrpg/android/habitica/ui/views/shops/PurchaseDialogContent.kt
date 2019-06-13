package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView

abstract class PurchaseDialogContent : LinearLayout {

    private val imageView: SimpleDraweeView by bindView(R.id.imageView)
    private val titleTextView: TextView by bindView(R.id.titleTextView)

    protected abstract val viewId: Int

    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView()
    }

    private fun setupView() {
        orientation = VERTICAL
        gravity = Gravity.CENTER

        inflate(context, viewId, this)
    }


    open fun setItem(item: ShopItem) {
        DataBindingUtils.loadImage(imageView, item.imageName)
        titleTextView.text = item.text
    }

    open fun setQuestContentItem(questContent: QuestContent) {
        DataBindingUtils.loadImage(imageView, "inventory_quest_scroll_" + questContent.key)
        titleTextView.text = questContent.text
    }
}
