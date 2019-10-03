package com.habitrpg.shared.habitica.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by keithholliday on 7/5/16.
 */
actual class PushDevice {

    @SerializedName("regId")
    @Expose
    actual var regId: String? = null

    @SerializedName("type")
    @Expose
    actual var type: String? = null
}
