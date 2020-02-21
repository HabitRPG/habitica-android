package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.models.AvatarPreferences
import com.habitrpg.shared.habitica.models.user.Hair
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation


open class MemberPreferences : NativeRealmObject(), AvatarPreferences {

    @PrimaryKeyAnnotation
    override var userId: String? = null

    override var hair: Hair? = null
    override var costume: Boolean = false
    override var disableClasses: Boolean = false
    override var sleep: Boolean = false
    override var shirt: String? = null
    override var skin: String? = null
    override var size: String? = null
    override var background: String? = null
    override var chair: String? = null

    fun setUserId(userId: String) {
        this.userId = userId
        if (hair != null && !hair!!.isManaged()) {
            hair!!.userId = userId
        }
    }

    fun getChair(): String? {
        return if (chair != null && chair != "none") {
            if (chair!!.length > 5 && chair!!.substring(0, 6) != "chair_") {
                chair
            } else {
                "chair_" + chair!!
            }
        } else null
    }
}
