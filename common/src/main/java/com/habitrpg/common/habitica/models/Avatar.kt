package com.habitrpg.common.habitica.models


interface Avatar {
    val currentMount: String?
    val currentPet: String?
    val sleep: Boolean
    val stats: AvatarStats?
    val preferences: AvatarPreferences?
    val flags: AvatarFlags?
    val gemCount: Int
    val hourglassCount: Int
    val costume: AvatarOutfit?
    val equipped: AvatarOutfit?
    val hasClass: Boolean
    fun isValid(): Boolean
}