package com.habitrpg.android.habitica.models.members


import com.habitrpg.shared.habitica.models.user.AvatarPreferences
import com.habitrpg.android.habitica.models.user.Hair

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MemberPreferences : RealmObject(), com.habitrpg.shared.habitica.models.user.AvatarPreferences {

    @PrimaryKey
    private var userId: String? = null

    private var hair: Hair? = null
    private var costume: Boolean = false
    private var disableClasses: Boolean = false
    private var sleep: Boolean = false
    private var shirt: String? = null
    private var skin: String? = null
    private var size: String? = null
    private var background: String? = null
    private var chair: String? = null

    fun setUserId(userId: String) {
        this.userId = userId
        if (hair != null && !hair!!.isManaged) {
            hair!!.userId = userId
        }
    }

    override fun getUserId(): String? {
        return userId
    }

    override fun getHair(): Hair? {
        return hair
    }

    fun setHair(hair: Hair) {
        this.hair = hair
    }

    override fun getCostume(): Boolean {
        return costume
    }

    fun setCostume(costume: Boolean) {
        this.costume = costume
    }

    override fun getDisableClasses(): Boolean {
        return disableClasses
    }

    fun setDisableClasses(disableClasses: Boolean) {
        this.disableClasses = disableClasses
    }

    override fun getSleep(): Boolean {
        return sleep
    }

    fun setSleep(sleep: Boolean) {
        this.sleep = sleep
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

    override fun getBackground(): String? {
        return background
    }

    fun setBackground(background: String) {
        this.background = background
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
}
