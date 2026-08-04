package com.habitrpg.common.habitica.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.habitrpg.common.habitica.BuildConfig
import com.habitrpg.common.habitica.helpers.KeyHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HostConfig {
    var address: String
    var port: String
    var apiKey: String = ""
    var userID: String

    // HostConfig is provided as a Hilt @Singleton, so this scope lives for the process lifetime.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val readySignal = CompletableDeferred<Unit>()

    val isInitialized: Boolean
        get() = readySignal.isCompleted

    suspend fun awaitReady() {
        readySignal.await()
    }

    constructor(userID: String, apiKey: String) {
        this.port = BuildConfig.PORT
        this.address = BuildConfig.BASE_URL
        this.userID = userID
        this.apiKey = apiKey
        readySignal.complete(Unit)
    }

    constructor(sharedPreferences: SharedPreferences, keyHelper: KeyHelper?, context: Context) {
        this.port = BuildConfig.PORT
        val address = sharedPreferences.getString("server_url", null)
        val addressValid = address.isNullOrBlank().not()
        if (BuildConfig.DEBUG) {
            this.address = if (addressValid) address else BuildConfig.BASE_URL
            if (BuildConfig.TEST_USER_ID.isNotBlank()) {
                userID = BuildConfig.TEST_USER_ID
                apiKey = BuildConfig.TEST_USER_KEY
                readySignal.complete(Unit)
                return
            }
        } else {
            if (addressValid) {
                this.address = address
            } else {
                this.address = context.getString(com.habitrpg.common.habitica.R.string.base_url)
            }
        }
        this.userID = sharedPreferences.getString(context.getString(com.habitrpg.common.habitica.R.string.SP_userID), null) ?: ""
        scope.launch {
            apiKey = loadAPIKey(sharedPreferences, keyHelper)
            readySignal.complete(Unit)
        }
    }

    private fun loadAPIKey(
        sharedPreferences: SharedPreferences,
        keyHelper: KeyHelper?,
    ): String =
        if (sharedPreferences.contains(userID)) {
            val encryptedKey = sharedPreferences.getString(userID, null)
            if (encryptedKey?.isNotBlank() == true) {
                keyHelper?.decrypt(encryptedKey)
            } else {
                ""
            }
        } else {
            val key = sharedPreferences.getString("APIToken", null)
            if (key?.isNotBlank() == true) {
                val encryptedKey = keyHelper?.encrypt(key)
                sharedPreferences.edit {
                    putString(userID, encryptedKey)
                    remove("APIToken")
                }
            }
            key
        } ?: ""

    constructor(address: String, port: String, api: String, user: String) {
        this.address = address
        this.port = port
        this.apiKey = api
        this.userID = user
        readySignal.complete(Unit)
    }

    fun hasAuthentication(): Boolean = userID.isNotEmpty() && apiKey.isNotEmpty()
}
