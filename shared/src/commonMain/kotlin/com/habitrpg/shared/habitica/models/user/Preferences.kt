package com.habitrpg.shared.habitica.models.user


import com.habitrpg.shared.habitica.models.AvatarPreferences
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class Preferences : NativeRealmObject(), AvatarPreferences {

    @PrimaryKeyAnnotation
    override var userId: String? = null
        set(userId) {
            field = userId
            if (hair?.isManaged() == false) {
                hair?.userId = userId
            }
            if (suppressModals?.isManaged() == false) {
                suppressModals?.userId = userId
            }
        }

    override var hair: Hair? = null
    var suppressModals: SuppressedModals? = null
    override var costume: Boolean = false
    @SerializedNameAnnotation("disableClasses")
    override var disableClasses: Boolean = false
    @SerializedNameAnnotation("sleep")
    override var sleep: Boolean = false
    var dailyDueDefaultView: Boolean = false
    var automaticAllocation: Boolean = false
    var allocationMode: String? = null
    override var shirt: String = ""
    override var skin: String = ""
    override var size: String = ""
    override var background: String? = null
    var language: String? = null
    var sound: String? = null
    var dayStart: Int = 0
    var timezoneOffset: Int = 0
    var timezoneOffsetAtLastCron: Int = 0
    var pushNotifications: PushNotificationsPreference? = null
    var emailNotifications: EmailNotificationsPreference? = null
    var autoEquip: Boolean = true

    override var chair: String? = null
        get(): String? {
            return if (field != null && field != "none") {
                if (field!!.length > 5 && field!!.substring(0, 6) != "chair_") {
                    field
                } else {
                    "chair_" + field!!
                }
            } else null
        }


    fun hasTaskBasedAllocation(): Boolean {
        return allocationMode?.toLowerCase() == "taskbased" && automaticAllocation
    }
}
