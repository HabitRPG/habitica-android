package com.habitrpg.android.habitica.ui.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShopGridSpacingDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutManager = parent.layoutManager as? GridLayoutManager ?: return
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val spanCount = layoutManager.spanCount
        val spanSize = layoutManager.spanSizeLookup.getSpanSize(position)

        if (spanSize >= spanCount) {
            outRect.setEmpty()
            return
        }

        val spanIndex = layoutManager.spanSizeLookup.getSpanIndex(position, spanCount)

        outRect.left = spacing * spanIndex / spanCount
        outRect.right = spacing * (spanCount - 1 - spanIndex) / spanCount
    }
}
