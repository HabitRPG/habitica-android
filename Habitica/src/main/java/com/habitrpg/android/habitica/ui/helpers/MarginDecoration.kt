package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R

class MarginDecoration(context: Context?, noMarginViewTypes: Set<Int>) : RecyclerView.ItemDecoration() {
    private val margin: Int = context?.resources?.getDimensionPixelSize(R.dimen.grid_item_margin) ?: 0
    private var noMarginViewTypes = noMarginViewTypes

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val view: View = parent.getChildAt(i)
            val position: Int = parent.getChildAdapterPosition(view)
            val viewType: Int? = parent.adapter?.getItemViewType(position)
            if (!noMarginViewTypes.contains(viewType)) {
                outRect.set(margin, margin, margin, margin)
            }
        }

    }
}
