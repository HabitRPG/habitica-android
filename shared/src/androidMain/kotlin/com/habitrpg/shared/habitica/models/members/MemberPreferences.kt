package com.habitrpg.shared.habitica.models.members


import com.habitrpg.shared.habitica.models.user.AvatarPreferences
import com.habitrpg.shared.habitica.models.user.Hair

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class MemberPreferences : RealmObject(), AvatarPreferences {

    @PrimaryKey
    actual override var userId: String? = null
        set(userId) {
            field = userId
            if (hair != null && !hair!!.isManaged) {
                hair!!.userId = userId
            }
        }

    actual override var hair: Hair? = null
    actual override var costume: Boolean = false
    actual override var isDisableClasses: Boolean = false
    actual override var isSleep: Boolean = false
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
}
