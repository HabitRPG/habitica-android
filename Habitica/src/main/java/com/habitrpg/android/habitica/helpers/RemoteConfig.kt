package com.habitrpg.android.habitica.helpers

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

interface RemoteConfig {
    fun getBoolean(key: String): Boolean

    fun getLong(key: String): Long

    fun getString(key: String): String
}

class FirebaseRemoteConfigSource
    @Inject
    constructor() : RemoteConfig {
        private val remoteConfig: FirebaseRemoteConfig
            get() = FirebaseRemoteConfig.getInstance()

        override fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)

        override fun getLong(key: String): Long = remoteConfig.getLong(key)

        override fun getString(key: String): String = remoteConfig.getString(key)
    }
