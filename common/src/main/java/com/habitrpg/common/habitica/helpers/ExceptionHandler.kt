package com.habitrpg.common.habitica.helpers

import android.util.Log
import coil.network.HttpException
import com.habitrpg.common.habitica.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

class ExceptionHandler {
    private var exceptionLogger: ((Throwable) -> Unit)? = null

    companion object {
        private var instance = ExceptionHandler()

        fun init(exceptionLogger: ((Throwable) -> Unit)? = null) {
            instance.exceptionLogger = exceptionLogger
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
                if (throwable !is CancellationException) {
                    instance.exceptionLogger?.invoke(throwable)
                }
            }
        }
    }
}

fun CoroutineScope.launchCatching(errorHandler: ((Throwable) -> Unit)? = null, function: suspend CoroutineScope.() -> Unit) {
    launch(
        (
            ExceptionHandler.coroutine {
                errorHandler?.invoke(it)
            }
            ),
        block = function
    )
}
