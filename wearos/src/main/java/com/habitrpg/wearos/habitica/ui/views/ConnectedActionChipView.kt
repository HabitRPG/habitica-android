package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class ConnectedActionChipView(
    context: Context,
    attrs: AttributeSet? = null,
) : TextActionChipView(context, attrs) {
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { info ->
            post {
                isEnabled = info.nodes.firstOrNull { it.isNearby } != null
                alpha = if (isEnabled) 1.0f else 0.5f
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        capabilityClient.addListener(capabilityListener, CAPABILITY)
    }

    override fun onDetachedFromWindow() {
        capabilityClient.removeListener(capabilityListener, CAPABILITY)
        super.onDetachedFromWindow()
    }

    private companion object {
        const val CAPABILITY = "open_activity"
    }
}
