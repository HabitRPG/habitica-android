package com.habitrpg.android.habitica.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PushDevice {
    @SerializedName("regId")
    @Expose
    var regId: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null
}