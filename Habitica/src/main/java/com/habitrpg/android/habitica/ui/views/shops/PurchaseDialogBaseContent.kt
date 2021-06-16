package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.DialogPurchaseContentItemBinding
import com.habitrpg.android.habitica.extensions.layoutInflater

class PurchaseDialogBaseContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseContentItemBinding.inflate(context.layoutInflater, this)
    override val imageView: ImageView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView
}
