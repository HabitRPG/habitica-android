package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.util.AttributeSet

import com.habitrpg.android.habitica.R

class PurchaseDialogBaseContent : PurchaseDialogContent {
    override val viewId: Int
        get() =  R.layout.dialog_purchase_content_item

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}
