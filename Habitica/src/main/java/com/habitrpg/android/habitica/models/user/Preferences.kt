package com.habitrpg.android.habitica.models.user


import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.AvatarPreferences

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Preferences : RealmObject(), AvatarPreferences {

    @PrimaryKey
    private var userId: String? = null

    private var hair: Hair? = null
    var suppressModals: SuppressedModals? = null
    private var costume: Boolean = false
    @SerializedName("disableClasses")
    var isDisableClasses: Boolean = false
    @SerializedName("sleep")
    var isSleep: Boolean = false
    var dailyDueDefaultView: Boolean = false
    var automaticAllocation: Boolean = false
    var allocationMode: String? = null
    private var shirt: String? = null
    private var skin: String? = null
    private var size: String? = null
    private var background: String? = null
    private var chair: String? = null
    var language: String? = null
    var sound: String? = null
    var dayStart: Int = 0
    var timezoneOffset: Int = 0
    var pushNotifications: PushNotificationsPreference? = null

    override fun getBackground(): String? {
        return background
    }

    fun setBackground(background: String) {
        this.background = background
    }

    override fun getCostume(): Boolean {
        return costume
    }

    fun setCostume(costume: Boolean) {
        this.costume = costume
    }

    override fun getDisableClasses(): Boolean {
        return isDisableClasses
    }

    override fun getSleep(): Boolean {
        return isSleep
    }

    override fun getShirt(): String? {
        return shirt
    }

    fun setShirt(shirt: String) {
        this.shirt = shirt
    }

    override fun getSkin(): String? {
        return skin
    }

    fun setSkin(skin: String) {
        this.skin = skin
    }

    override fun getSize(): String? {
        return size
    }

    fun setSize(size: String) {
        this.size = size
    }

    override fun getHair(): Hair? {
        return hair
    }

    fun setHair(hair: Hair) {
        this.hair = hair
    }

    override fun getChair(): String? {
        return if (chair != null && chair != "none") {
            if (chair!!.length > 5 && chair!!.substring(0, 6) != "chair_") {
                chair
            } else {
                "chair_" + chair!!
            }
        } else null
    }

    fun setChair(chair: String) {
        this.chair = chair
    }

    override fun getUserId(): String? {
        return userId
    }

    fun setUserId(userId: String?) {
        this.userId = userId
        if (hair?.isManaged == false) {
            hair?.userId = userId
        }
        if (suppressModals?.isManaged == false) {
            suppressModals?.userId = userId
        }
    }

    fun hasTaskBasedAllocation(): Boolean {
        return allocationMode?.toLowerCase() == "taskbased" && automaticAllocation
    }
}
