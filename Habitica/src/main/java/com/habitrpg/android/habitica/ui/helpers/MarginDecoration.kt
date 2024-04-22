package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R

class MarginDecoration(context: Context?, private var noMarginViewTypes: Set<Int> = setOf()) :
    RecyclerView.ItemDecoration() {
    private val margin: Int =
        context?.resources?.getDimensionPixelSize(R.dimen.grid_item_margin) ?: 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        val viewType: Int? = parent.adapter?.getItemViewType(position)
        if (noMarginViewTypes.contains(viewType)) {
            outRect.setEmpty()
        } else {
            outRect.set(margin, margin, margin, margin)
        }
    }
}
