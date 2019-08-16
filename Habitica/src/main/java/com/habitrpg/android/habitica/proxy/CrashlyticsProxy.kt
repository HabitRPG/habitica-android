package com.habitrpg.android.habitica.proxy


import android.content.Context

interface CrashlyticsProxy {
    fun init(context: Context)

    fun logException(t: Throwable)

    fun setString(key: String, value: String)

    fun setUserIdentifier(identifier: String)

    fun setUserName(name: String)

    fun fabricLogE(s1: String, s2: String, e: Exception)

    fun log(msg: String)
}
