package com.habitrpg.android.habitica.helpers

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RemoteConfigManager {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun repeatablesAreEnabled(): Boolean {
        return true
    }

    fun newShopsEnabled(): Boolean {
        return true
    }

    fun shopSpriteSuffix(): String {
        return remoteConfig.getString("shopSpriteSuffix")
    }

    fun maxChatLength(): Long {
        return remoteConfig.getLong("maxChatLength")
    }

    fun enableGiftOneGetOne(): Boolean {
        return remoteConfig.getBoolean("enableGiftOneGetOne")
    }

    fun spriteSubstitutions(): Map<String, Map<String, String>> {
        val type = object : TypeToken<Map<String, Map<String, String>>>() {}.type
        return Gson().fromJson(remoteConfig.getString("spriteSubstitutions"), type)
    }

    fun supportEmail(): String {
        return remoteConfig.getString("supportEmail")
    }

    fun enableUsernameAutocomplete(): Boolean {
        return remoteConfig.getBoolean("enableUsernameAutocomplete")
    }

    fun enableLocalChanges(): Boolean {
        return remoteConfig.getBoolean("enableLocalChanges")
    }
}
