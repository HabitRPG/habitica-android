package com.habitrpg.android.habitica.helpers

import android.util.Log
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import io.reactivex.rxjava3.functions.Consumer
import okhttp3.internal.http2.ConnectionShutdownException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException

class RxErrorHandler {
    private var crashlyticsProxy: CrashlyticsProxy? = null

    companion object {

        private var instance: RxErrorHandler? = null

        fun init(crashlyticsProxy: CrashlyticsProxy) {
            instance = RxErrorHandler()
            instance?.crashlyticsProxy = crashlyticsProxy
        }

        fun handleEmptyError(): Consumer<Throwable> {
            //Can't be turned into a lambda, because it then doesn't work for some reason.
            return Consumer { reportError(it) }
        }

        fun reportError(throwable: Throwable) {
            if (BuildConfig.DEBUG) {
                try {
                    Log.e("ObservableError", Log.getStackTraceString(throwable))
                } catch (ignored: Exception) {
                }

            } else {
                if (!IOException::class.java.isAssignableFrom(throwable.javaClass)
                        && !HttpException::class.java.isAssignableFrom(throwable.javaClass)
                        && !retrofit2.HttpException::class.java.isAssignableFrom(throwable.javaClass)
                        && !EOFException::class.java.isAssignableFrom(throwable.javaClass)
                        && throwable !is ConnectionShutdownException) {
                    instance?.crashlyticsProxy?.logException(throwable)
                }
            }
        }
    }
}
