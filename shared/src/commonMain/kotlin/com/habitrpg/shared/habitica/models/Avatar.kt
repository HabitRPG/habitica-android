package com.habitrpg.shared.habitica.models

interface Avatar {
    val id: String?
    val balance: Double
    val authentication: AvatarAuthentication?
    val stats: AvatarStats?
    val preferences: AvatarPreferences?
    val flags: AvatarFlags?
    val items: AvatarItems?
    val hourglassCount: Int
    fun isValid(): Boolean

    val username: String?
        get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    val gemCount: Int
        get() = (this.balance * 4).toInt()

    val costume: AvatarOutfit?
        get() = items?.gear?.costume

    val equipped: AvatarOutfit?
        get() = items?.gear?.equipped

    val hasClass: Boolean
        get() {
            return preferences?.disableClasses != true &&
                flags?.classSelected != false &&
                stats?.habitClass?.isNotEmpty() == true &&
                (stats?.lvl ?: 0) >= 10
        }

    val currentMount: String?
        get() = items?.currentMount ?: ""
    val currentPet: String?
        get() = items?.currentPet ?: ""

    val sleep: Boolean
        get() = preferences?.sleep ?: false
}
