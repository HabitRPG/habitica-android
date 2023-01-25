package com.habitrpg.android.habitica.helpers

import android.util.Log
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.StreamResetException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ExceptionHandler {
    private var analyticsManager: AnalyticsManager? = null

    companion object {

        private var instance = ExceptionHandler()

        fun init(analyticsManager: AnalyticsManager) {
            instance.analyticsManager = analyticsManager
        }

        fun coroutine(handler: ((Throwable) -> Unit)? = null): CoroutineExceptionHandler {
            return CoroutineExceptionHandler { _, throwable ->
                reportError(throwable)
                handler?.invoke(throwable)
            }
        }

        fun reportError(throwable: Throwable) {
            if (BuildConfig.DEBUG) {
                try {
                    Log.e("ObservableError", Log.getStackTraceString(throwable))
                } catch (ignored: Exception) {
                }
            } else {
                if (throwable !is IOException &&
                    throwable !is HttpException &&
                    throwable !is CancellationException
                ) {
                    instance.analyticsManager?.logException(throwable)
                }
            }
        }
    }
}

fun CoroutineScope.launchCatching(errorHandler: ((Throwable) -> Unit)? = null, function: suspend CoroutineScope.() -> Unit) {
    launch((ExceptionHandler.coroutine {
        errorHandler?.invoke(it)
    }), block = function)
}