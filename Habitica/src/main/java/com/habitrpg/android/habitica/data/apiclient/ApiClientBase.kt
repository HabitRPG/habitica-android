package com.habitrpg.android.habitica.data.apiclient

import com.google.gson.JsonSyntaxException
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.implementation.ConnectionProblemDialogs
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.common.habitica.models.HabitResponse
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

abstract class ApiClientBase(
    private val notificationsManager: NotificationsManager,
    private val dialogs: ConnectionProblemDialogs
    ) : ApiClient {

    fun <T> processResponse(habitResponse: HabitResponse<T>): T? {
        habitResponse.notifications?.let {
            notificationsManager.setNotifications(it)
        }
        return habitResponse.data
    }

    suspend fun <T> process(apiCall: suspend () -> HabitResponse<T>): T? {
        try {
            return processResponse(apiCall())
        } catch (throwable: Throwable) {
            accept(throwable)
        }
        return null
    }

    fun accept(throwable: Throwable) {
        val throwableClass = throwable.javaClass
        if (SocketTimeoutException::class.java.isAssignableFrom(throwableClass)) {
            return
        }

        var isUserInputCall = false
        if (SocketException::class.java.isAssignableFrom(throwableClass) ||
            SSLException::class.java.isAssignableFrom(throwableClass)
        ) {
            dialogs.showConnectionProblemDialog(
                R.string.internal_error_api, isUserInputCall)
        } else if (throwableClass == SocketTimeoutException::class.java
            || UnknownHostException::class.java == throwableClass
            || IOException::class.java == throwableClass) {
            dialogs.showConnectionProblemDialog(
                R.string.network_error_no_network_body,
                isUserInputCall,
            )
        } else if (HttpException::class.java.isAssignableFrom(throwable.javaClass)) {
            val error = throwable as HttpException
            val res = getErrorResponse(error)
            val status = error.code()
            val requestUrl = error.response()?.raw()?.request?.url
            val path = requestUrl?.encodedPath?.removePrefix("/api/v4") ?: ""
            isUserInputCall =
                when {
                    path.startsWith("/groups") && path.endsWith("invite") -> true
                    else -> false
                }

            if (res.message != null && res.message == "RECEIPT_ALREADY_USED") {
                return
            }
            if (requestUrl?.toString()?.endsWith("/user/push-devices") == true) {
                // workaround for an error that sometimes displays that the user already has this push device
                return
            }

            if (status in 400..499) {
                if (res.displayMessage.isNotEmpty()) {
                    dialogs.showConnectionProblemDialog("", res.displayMessage, isUserInputCall)
                } else if (status == 401) {
                    dialogs.showConnectionProblemDialog(
                        R.string.authentication_error_title,
                        R.string.authentication_error_body,
                        isUserInputCall,
                    )
                }
            } else if (status in 500..599) {
                dialogs.showConnectionProblemDialog(R.string.internal_error_api, isUserInputCall)
            } else {
                dialogs.showConnectionProblemDialog(R.string.internal_error_api, isUserInputCall)
            }
        } else if (JsonSyntaxException::class.java.isAssignableFrom(throwableClass)) {
            Analytics.logError("Json Error: " +
                  //  lastAPICallURL +                     //todo, maybe write it to shared pref
                    ",  " + throwable.message)
        } else {
            Analytics.logException(throwable)
        }
    }
}