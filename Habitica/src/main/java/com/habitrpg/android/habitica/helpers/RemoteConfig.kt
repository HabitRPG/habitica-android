package com.habitrpg.android.habitica.helpers

interface RemoteConfig {
    fun getBoolean(key: String): Boolean

    fun getLong(key: String): Long

    fun getString(key: String): String
}

