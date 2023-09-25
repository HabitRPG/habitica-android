package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ConnectedActionChipView(context: Context, attrs: AttributeSet? = null) :
    TextActionChipView(context, attrs) {
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }

    init {
        checkIfPhoneAvailable()
    }

    private fun checkIfPhoneAvailable() {
        MainScope().launch(Dispatchers.IO) {
            capabilityClient.addListener( {
                launch(Dispatchers.Main) {
                    isEnabled = it.nodes.firstOrNull { it.isNearby } != null
                    alpha = if (isEnabled) 1.0f else 0.7f
                }
            }, "open_activity")
        }
    }
}
