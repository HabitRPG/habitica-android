package com.habitrpg.android.habitica.ui.menu

import android.content.Context
import android.view.View
import com.habitrpg.android.habitica.databinding.MenuBottomSheetBinding
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog

class BottomSheetMenu(context: Context) : HabiticaBottomSheetDialog(context), View.OnClickListener {
    private var binding = MenuBottomSheetBinding.inflate(layoutInflater)
    private var runnable: ((Int) -> Unit)? = null

    init {
        setContentView(binding.root)
        binding.titleView.visibility = View.GONE
    }

    fun setSelectionRunnable(runnable: (Int) -> Unit) {
        this.runnable = runnable
    }

    override fun setTitle(title: CharSequence?) {
        binding.titleView.text = title
        binding.titleView.visibility = View.VISIBLE
        binding.titleView.background = null
    }

    fun setImage(url: String) {
        binding.iconView.loadImage(url)
        binding.iconView.visibility = View.VISIBLE
    }

    fun addMenuItem(menuItem: BottomSheetMenuItem) {
        val item = menuItem.inflate(this.context, layoutInflater, this.binding.menuItems)
        item.setOnClickListener(this)
        this.binding.menuItems.addView(item)
        binding.root.requestLayout()
    }

    override fun onClick(v: View) {
        if (this.runnable != null) {
            val index = this.binding.menuItems.indexOfChild(v)
            if (index != -1) {
                runnable?.let { it(index) }
                this.dismiss()
            }
        }
    }
}
