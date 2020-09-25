package com.habitrpg.android.habitica.models.members


import com.habitrpg.android.habitica.models.AvatarPreferences
import com.habitrpg.android.habitica.models.user.Hair

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MemberPreferences : RealmObject(), AvatarPreferences {

    @PrimaryKey
    override var userId: String? = null
    set(value) {
        field = value
        if (hair?.isManaged != true) {
            hair?.userId = userId
        }
    }

    override var hair: Hair? = null
    override var costume: Boolean = false
    override var disableClasses: Boolean = false
    override var sleep: Boolean = false
    override var shirt: String? = null
    override var skin: String? = null
    override var size: String? = null
    override var background: String? = null
    override var chair: String? = null
    get() {
        return if (field != null && field != "none") {
            if (field!!.length > 5 && field?.substring(0, 6) != "chair_") {
                field
            } else {
                "chair_$field"
            }
        } else null
    }

}
