package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.nativePackages.annotations.ExposeAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

/**
 * Created by keithholliday on 7/5/16.
 */
class PushDevice {

    @SerializedNameAnnotation("regId")
    @ExposeAnnotation
    var regId: String? = null

    @SerializedNameAnnotation("type")
    @ExposeAnnotation
    var type: String? = null
}
