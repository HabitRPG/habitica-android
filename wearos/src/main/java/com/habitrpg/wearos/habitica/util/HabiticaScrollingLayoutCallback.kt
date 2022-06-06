package com.habitrpg.wearos.habitica.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import kotlin.math.abs

private const val MAX_ICON_PROGRESS = 0.8f

class HabiticaScrollingLayoutCallback : WearableLinearLayoutManager.LayoutCallback() {

    private var progressToCenter: Float = 0f

    override fun onLayoutFinished(child: View, parent: RecyclerView) {
        child.apply {
            // Figure out % progress from top to bottom
            val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
            val yRelativeToCenterOffset = y / parent.height + centerOffset

            // Normalize for center
            progressToCenter = abs(0.5f - yRelativeToCenterOffset) - 0.25f
            if (progressToCenter < 0) {
                scaleX = 1f
                scaleY = 1f
                alpha = 1f
                return
            }
            // Adjust to the maximum scale
            progressToCenter = Math.min(progressToCenter * 1.5f, MAX_ICON_PROGRESS)

            scaleX = 1 - progressToCenter
            scaleY = 1 - progressToCenter
            alpha = 1 - progressToCenter * 2
        }
    }
}