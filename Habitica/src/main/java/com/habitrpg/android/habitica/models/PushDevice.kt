package com.habitrpg.android.habitica.models

import kotlinx.serialization.Serializable

@Serializable
class PushDevice {
    var regId: String? = null
    var type: String? = null
}
