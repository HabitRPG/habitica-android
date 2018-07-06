package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.models.shops.ShopItem

internal class PurchaseDialogGemsContent(context: Context) : PurchaseDialogContent(context) {

    val notesTextView: TextView by bindView(R.id.notesTextView)

    override val viewId: Int
        get() = R.layout.dialog_purchase_content_item


    override fun setItem(item: ShopItem) {
        super.setItem(item)

        notesTextView.text = item.notes
    }
}
