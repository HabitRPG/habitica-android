package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Stats

/**
 * Created by phillip on 29.06.17.
 */
interface Avatar {
    val currentMount: String?
    val currentPet: String?
    val sleep: Boolean
    val stats: Stats?
    val preferences: AvatarPreferences?
    val flags: AvatarFlags?
    val gemCount: Int
    val hourglassCount: Int
    val costume: Outfit?
    val equipped: Outfit?
    val hasClass: Boolean
    fun isValid(): Boolean
}
