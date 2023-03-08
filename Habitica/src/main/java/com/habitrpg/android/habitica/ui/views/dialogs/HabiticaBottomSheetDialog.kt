package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.BottomSheetWrapperBinding

open class HabiticaBottomSheetDialog(context: Context) : BottomSheetDialog(context, R.style.SheetDialog) {
    private val wrapperBinding = BottomSheetWrapperBinding.inflate(layoutInflater)

    init {
        behavior.peekHeight = context.resources.displayMetrics.heightPixels / 2
    }

    var grabberVisibility: Int
        get() = wrapperBinding.grabber.visibility
        set(value) {
            wrapperBinding.grabber.visibility = value
        }

    override fun setContentView(view: View) {
        wrapperBinding.container.addView(view)
        super.setContentView(wrapperBinding.root)
    }

    override fun setContentView(layoutResID: Int) {
        layoutInflater.inflate(layoutResID, wrapperBinding.container)
        super.setContentView(wrapperBinding.root)
    }
}
