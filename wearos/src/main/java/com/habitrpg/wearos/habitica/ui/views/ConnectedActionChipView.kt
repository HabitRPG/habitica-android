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
            val result = Tasks.await(capabilityClient.getCapability("open_activity", CapabilityClient.FILTER_REACHABLE))
            launch(Dispatchers.Main) {
                isEnabled = result.nodes.firstOrNull { it.isNearby } != null
                isVisible = isEnabled
            }
        }
    }
}
