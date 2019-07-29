package com.habitrpg.android.habitica.api

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.KeyHelper


/**
 * The configuration of the host<br></br>
 * Currently, the Port isn't used at all.
 *
 * @author MagicMicky
 */
class HostConfig {
    var address: String
    var port: String
    var apiKey: String
    var userID: String

    constructor(sharedPreferences: SharedPreferences, keyHelper: KeyHelper?, context: Context) {
        this.port = BuildConfig.PORT
        if (BuildConfig.DEBUG) {
            this.address = BuildConfig.BASE_URL
        } else {
            val address = sharedPreferences.getString("server_url", null)
            if (address != null && address.isNotEmpty()) {
                this.address = address
            } else {
                this.address = context.getString(R.string.base_url)
            }
        }
        this.userID = sharedPreferences.getString(context.getString(R.string.SP_userID), null) ?: ""

        this.apiKey = loadAPIKey(sharedPreferences, keyHelper)
    }

    private fun loadAPIKey(sharedPreferences: SharedPreferences, keyHelper: KeyHelper?): String {
        return if (sharedPreferences.contains(userID)) {
            val encryptedKey = sharedPreferences.getString(userID, null)
            if (encryptedKey?.isNotBlank() == true) {
                keyHelper?.decrypt(encryptedKey)
            } else {
                ""
            }
        } else {
            val key = sharedPreferences.getString("APIToken", null)
            if (key?.isNotBlank() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val encryptedKey = keyHelper?.encrypt(key)
                sharedPreferences.edit {
                    putString(userID, encryptedKey)
                    remove("APIToken")
                }
            }
            key
        } ?: ""
    }

    constructor(address: String, port: String, api: String, user: String) {
        this.address = address
        this.port = port
        this.apiKey = api
        this.userID = user
    }

    fun hasAuthentication(): Boolean {
        return userID.isNotEmpty() && apiKey.isNotEmpty()
    }

}

