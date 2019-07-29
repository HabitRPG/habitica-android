package com.habitrpg.android.habitica.ui.menu

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.habitrpg.android.habitica.R

class BottomSheetMenu(context: Context) : BottomSheetDialog(context), View.OnClickListener {
    private var contentView = layoutInflater.inflate(R.layout.menu_bottom_sheet, null) as LinearLayout
    private var runnable: ((Int) -> Unit)? = null

    init {
        setContentView(contentView)
    }

    fun setSelectionRunnable(runnable: (Int) -> Unit) {
        this.runnable = runnable
    }

    fun addMenuItems(vararg menuItems: BottomSheetMenuItem) {
        for (menuItem in menuItems) {
            this.addMenuItem(menuItem)
        }
    }

    fun addMenuItem(menuItem: BottomSheetMenuItem) {
        val item = menuItem.inflate(this.context, layoutInflater, this.contentView)
        item.setOnClickListener(this)
        this.contentView.addView(item)
    }

    fun removeMenuItem(index: Int) {
        this.contentView.removeViewAt(index)
    }

    override fun onClick(v: View) {
        if (this.runnable != null) {
            val index = this.contentView.indexOfChild(v)
            if (index != -1) {
                runnable?.let { it(index) }
                this.dismiss()
            }
        }
    }
}
