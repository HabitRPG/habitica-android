package com.habitrpg.android.habitica.proxy.implementation


import com.habitrpg.android.habitica.proxy.CrashlyticsProxy

class EmptyCrashlyticsProxy : CrashlyticsProxy {

    override fun logException(e: Throwable) {
        //pass
    }

    override fun setUserIdentifier(identifier: String) {
        //pass
    }

    override fun log(msg: String) {
        //pass
    }
}
