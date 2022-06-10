package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.wear.widget.WearableRecyclerView

class HabiticaRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WearableRecyclerView(context, attrs) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            setPaddingRelative(0, (height * 0.06).toInt(), 0, (height * 0.25).toInt())
            scrollToPosition(0)
        }
    }
}