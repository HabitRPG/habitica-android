package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.user.Hair

interface AvatarPreferences {
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
