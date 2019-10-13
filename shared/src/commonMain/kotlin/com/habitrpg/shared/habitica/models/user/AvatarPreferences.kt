package com.habitrpg.shared.habitica.models.user

/**
 * Created by phillip on 15.09.17.
 */

interface AvatarPreferences {
    var userId: String?

    val hair: Hair?

    val costume: Boolean?

    val isSleep: Boolean

    val shirt: String?

    val skin: String?
    val size: String?

    val background: String?

    val chair: String?

    val isDisableClasses: Boolean?
}
