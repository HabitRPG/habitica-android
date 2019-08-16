package com.habitrpg.android.habitica.ui.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R

class BottomSheetMenuItem {

    private var title: String? = null
    private var isDestructive: Boolean? = null

    constructor(title: String) {
        this.title = title
        this.isDestructive = false
    }

    constructor(title: String, isDestructive: Boolean?) {
        this.title = title
        this.isDestructive = isDestructive
    }

    fun inflate(context: Context, inflater: LayoutInflater, contentView: ViewGroup): View {
        val menuItemView = inflater.inflate(R.layout.menu_bottom_sheet_item, contentView, false) as? LinearLayout
        val textView = menuItemView?.findViewById<TextView>(R.id.textView)
        textView?.text = this.title
        if (this.isDestructive == true) {
            textView?.setTextColor(ContextCompat.getColor(context, R.color.red_50))
        }
        return menuItemView ?: View(context)
    }
}
