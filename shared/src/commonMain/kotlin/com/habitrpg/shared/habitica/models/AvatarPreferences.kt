package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.models.user.Hair

/**
 * Created by phillip on 15.09.17.
 */

interface AvatarPreferences {

    val userId: String?

    val hair: Hair?

    val costume: Boolean

    val sleep: Boolean

    val shirt: String?

    val skin: String?
    val size: String?

    val background: String?

    val chair: String?

    val disableClasses: Boolean
}
