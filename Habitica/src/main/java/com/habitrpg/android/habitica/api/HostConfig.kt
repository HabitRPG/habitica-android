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
    var api: String? = null
    var user: String? = null

    constructor(sharedPreferences: SharedPreferences, context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        this.port = BuildConfig.PORT
        if (BuildConfig.DEBUG) {
            this.address = BuildConfig.BASE_URL
        } else {
            this.address = sharedPreferences.getString("server_url", null) ?: context.getString(R.string.base_url)
        }
        this.api = prefs.getString("APIToken", null)
        this.user = prefs.getString(context.getString(R.string.SP_userID), "")
    }

    constructor(address: String, port: String, api: String, user: String) {
        this.address = address
        this.port = port
        this.api = api
        this.user = user
    }

    fun hasAuthentication(): Boolean {
        return user?.isNotEmpty() == true && api?.length ?: 0 > 0
    }

}

