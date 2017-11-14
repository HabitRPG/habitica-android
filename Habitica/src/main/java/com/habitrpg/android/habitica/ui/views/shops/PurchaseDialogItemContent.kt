package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.models.shops.ShopItem

class PurchaseDialogItemContent : PurchaseDialogContent {

    internal val notesTextView: TextView by bindView(R.id.notesTextView)

    override val viewId: Int
        get() = R.layout.dialog_purchase_content_item

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun setItem(item: ShopItem) {
        super.setItem(item)
        notesTextView.text = item.notes
    }
}
