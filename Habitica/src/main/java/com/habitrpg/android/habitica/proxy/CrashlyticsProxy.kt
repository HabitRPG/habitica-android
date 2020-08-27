package com.habitrpg.android.habitica.proxy


interface CrashlyticsProxy {
    fun logException(t: Throwable)

    fun setUserIdentifier(identifier: String)

    fun log(msg: String)
}
