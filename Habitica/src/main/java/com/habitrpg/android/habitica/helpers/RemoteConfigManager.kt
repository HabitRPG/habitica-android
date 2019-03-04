package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.os.AsyncTask
import androidx.preference.PreferenceManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request

import org.json.JSONException
import org.json.JSONObject
import java.io.*

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
}
