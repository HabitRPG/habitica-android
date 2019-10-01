package com.habitrpg.shared.habitica.models.user


expect open class Preferences : AvatarPreferences {

    override var userId: String?

    override var hair: Hair?
    var suppressModals: SuppressedModals?
    override var costume: Boolean
    override var isDisableClasses: Boolean
    override var isSleep: Boolean
    var dailyDueDefaultView: Boolean
    var automaticAllocation: Boolean
    var allocationMode: String?
    override var shirt: String?
    override var skin: String?
    override var size: String?
    override var background: String?
    override var chair: String?
    var language: String?
    var sound: String?
    var dayStart: Int
    var timezoneOffset: Int
    var timezoneOffsetAtLastCron: Int
    var pushNotifications: PushNotificationsPreference?
    var autoEquip: Boolean

    fun hasTaskBasedAllocation(): Boolean
}
