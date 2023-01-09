package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarPreferences
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Preferences : RealmObject(), AvatarPreferences, BaseObject {
    override var hair: Hair? = null
    var suppressModals: SuppressedModals? = null
    override var costume: Boolean = false
    @SerializedName("disableClasses")
    override var disableClasses: Boolean = false
    @SerializedName("sleep")
    override var sleep: Boolean = false
    var dailyDueDefaultView: Boolean = false
    var automaticAllocation: Boolean = false
    var allocationMode: String? = null
    override var shirt: String? = null
    override var skin: String? = null
    override var size: String? = null
    override var background: String? = null
    override var chair: String? = null
        get() {
            return if (field != null && field != "none") {
                if (field?.contains("chair_") == true) {
                    field
                } else {
                    "chair_" + field!!
                }
            } else "chair_none"
        }
    var language: String? = null
    var sound: String? = null
    var dayStart: Int = 0
    var timezoneOffset: Int = 0
    var timezoneOffsetAtLastCron: Int = 0
    var pushNotifications: PushNotificationsPreference? = null
    var emailNotifications: EmailNotificationsPreference? = null
    var autoEquip: Boolean = true
    var tasks: UserTaskPreferences? = null
}
