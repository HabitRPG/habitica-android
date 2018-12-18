package com.habitrpg.android.habitica.api

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R

/**
 * The configuration of the host<br></br>
 * Currently, the Port isn't used at all.
 *
 * @author MagicMicky
 */
class HostConfig {
    var address: String
    var port: String
    var api: String
    var user: String

    constructor(sharedPreferences: SharedPreferences, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
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
        this.api = prefs.getString("APIToken", null) ?: ""
        this.user = prefs.getString(context.getString(R.string.SP_userID), null) ?: ""
    }

    constructor(address: String, port: String, api: String, user: String) {
        this.address = address
        this.port = port
        this.api = api
        this.user = user
    }

    fun hasAuthentication(): Boolean {
        return user.isNotEmpty() && api.isNotEmpty()
    }

}

