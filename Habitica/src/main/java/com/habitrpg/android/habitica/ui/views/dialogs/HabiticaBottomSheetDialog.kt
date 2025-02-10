package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.BottomSheetWrapperBinding
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30

open class HabiticaBottomSheetDialog(context: Context) :
    BottomSheetDialog(context, R.style.SheetDialog) {
    private val wrapperBinding = BottomSheetWrapperBinding.inflate(layoutInflater)

    init {
        behavior.peekHeight = context.resources.displayMetrics.heightPixels / 2
        ViewCompat.setOnApplyWindowInsetsListener(wrapperBinding.container) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom)
            consumeWindowInsetsAbove30(windowInsets)
        }
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
