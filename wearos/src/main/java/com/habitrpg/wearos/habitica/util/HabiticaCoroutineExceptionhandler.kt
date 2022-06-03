package com.habitrpg.wearos.habitica.util

import android.util.Log
import com.habitrpg.wearos.habitica.models.DisplayedError
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject

@ViewModelScoped
class ExceptionHandlerBuilder @Inject constructor() {
    fun silent(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { context, throwable ->
            Log.e(context.toString(), "Error: ${throwable.cause}", throwable)
        }
    }

    fun userFacing(errorPresenter: ErrorPresenter): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            errorPresenter.errorValues.value = throwable.message?.let { DisplayedError(it) }
        }
    }
}