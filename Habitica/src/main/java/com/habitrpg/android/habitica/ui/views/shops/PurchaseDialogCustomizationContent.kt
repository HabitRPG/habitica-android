package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.DialogPurchaseCustomizationBinding
import com.habitrpg.android.habitica.extensions.layoutInflater

class PurchaseDialogCustomizationContent(context: Context) : PurchaseDialogContent(context) {
    val binding = DialogPurchaseCustomizationBinding.inflate(context.layoutInflater, this)
    override val imageView: ImageView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView
}