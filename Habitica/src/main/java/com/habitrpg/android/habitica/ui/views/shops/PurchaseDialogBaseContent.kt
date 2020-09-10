package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.databinding.DialogPurchaseContentItemBinding
import com.habitrpg.android.habitica.extensions.layoutInflater

class PurchaseDialogBaseContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseContentItemBinding.inflate(context.layoutInflater, this)
    override val imageView: SimpleDraweeView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView
}
