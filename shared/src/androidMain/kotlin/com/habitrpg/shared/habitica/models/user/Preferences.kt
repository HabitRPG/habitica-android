package com.habitrpg.shared.habitica.models.user


import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.models.user.AvatarPreferences

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Preferences : RealmObject(), AvatarPreferences {

    @PrimaryKey
    actual override var userId: String? = null
        set(userId: String?) {
            field = userId
            if (hair?.isManaged == false) {
                hair?.userId = userId
            }
            if (suppressModals?.isManaged == false) {
                suppressModals?.userId = userId
            }
        }

    actual override var hair: Hair? = null
    actual var suppressModals: SuppressedModals? = null
    actual override var costume: Boolean = false
    @SerializedName("disableClasses")
    actual override var isDisableClasses: Boolean = false
    @SerializedName("sleep")
    actual override var isSleep: Boolean = false
    actual var dailyDueDefaultView: Boolean = false
    actual var automaticAllocation: Boolean = false
    actual var allocationMode: String? = null
    actual override var shirt: String? = null
    actual override var skin: String? = null
    actual override var size: String? = null
    actual override var background: String? = null
    actual override var chair: String? = null
        get() {
            return if (field != null && field != "none") {
                if (field!!.length > 5 && field!!.substring(0, 6) != "chair_") {
                    field
                } else {
                    "chair_" + field!!
                }
            } else null
        }
    actual var language: String? = null
    actual var sound: String? = null
    actual var dayStart: Int = 0
    actual var timezoneOffset: Int = 0
    actual var timezoneOffsetAtLastCron: Int = 0
    actual var pushNotifications: PushNotificationsPreference? = null
    actual var autoEquip: Boolean = true

    actual fun hasTaskBasedAllocation(): Boolean {
        return allocationMode?.toLowerCase() == "taskbased" && automaticAllocation
    }
}
