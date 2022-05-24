package com.habitrpg.android.habitica.ui.menu

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.MenuBottomSheetItemBinding

class BottomSheetMenuItem(private val title: String, private val isDestructive: Boolean = false, private val currency: String? = null, private val price: Double = 0.0) {

    fun inflate(context: Context, inflater: LayoutInflater, contentView: ViewGroup): View {
        val binding = MenuBottomSheetItemBinding.inflate(inflater, contentView, false)
        binding.textView.text = this.title
        if (this.isDestructive) {
            binding.root.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.maroon_100))
        }
        if (price > 0) {
            binding.currencyView.currency = currency ?: "gold"
            binding.currencyView.value = price
            binding.currencyView.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        return binding.root
    }
}
