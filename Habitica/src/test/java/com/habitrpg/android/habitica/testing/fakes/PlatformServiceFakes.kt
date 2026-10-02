package com.habitrpg.android.habitica.testing.fakes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import com.habitrpg.android.habitica.helpers.AnalyticsManager
import com.habitrpg.android.habitica.helpers.CrashReporter
import com.habitrpg.android.habitica.helpers.PerformanceMonitor
import com.habitrpg.android.habitica.helpers.PerformanceTrace
import com.habitrpg.android.habitica.helpers.RemoteConfig
import com.habitrpg.common.habitica.helpers.Navigator

class FakeNavigator : Navigator {
    sealed interface Navigation {
        data class ToDestination(
            val destinationId: Int,
            val args: Bundle?,
        ) : Navigation

        data class ToDirections(
            val directions: NavDirections,
        ) : Navigation

        data class ToUri(
            val uri: String,
        ) : Navigation

        data class ToDeepLink(
            val request: NavDeepLinkRequest,
        ) : Navigation

        data class HandleDeepLink(
            val intent: Intent,
        ) : Navigation

        data object Back : Navigation
    }

    val navigations = mutableListOf<Navigation>()

    val lastNavigation: Navigation?
        get() = navigations.lastOrNull()

    override fun navigate(
        transactionId: Int,
        args: Bundle?,
    ) {
        navigations += Navigation.ToDestination(transactionId, args)
    }

    override fun navigate(directions: NavDirections) {
        navigations += Navigation.ToDirections(directions)
    }

    override fun navigate(uriString: String) {
        navigations += Navigation.ToUri(uriString)
    }

    override fun navigate(uri: Uri) {
        navigations += Navigation.ToUri(uri.toString())
    }

    override fun navigate(request: NavDeepLinkRequest) {
        navigations += Navigation.ToDeepLink(request)
    }

    override fun handle(deeplink: Intent) {
        navigations += Navigation.HandleDeepLink(deeplink)
    }

    override fun navigateBack() {
        navigations += Navigation.Back
    }

    fun reset() = navigations.clear()
}

class FakeRemoteConfig : RemoteConfig {
    val values = mutableMapOf<String, Any>()

    override fun getBoolean(key: String): Boolean = values[key] as? Boolean ?: false

    override fun getLong(key: String): Long = (values[key] as? Number)?.toLong() ?: 0L

    override fun getString(key: String): String = values[key]?.toString() ?: ""

    fun reset() = values.clear()
}

class FakeAnalyticsManager : AnalyticsManager {
    var currentUserID: String? = null
    var hasConsent: Boolean? = null
    val loggedErrors = mutableListOf<String>()
    val loggedExceptions = mutableListOf<Throwable>()

    override fun initialize(context: Context) = Unit

    override fun setUserID(userID: String) {
        currentUserID = userID
    }

    override fun clearUserID() {
        currentUserID = null
    }

    override fun logError(msg: String) {
        loggedErrors += msg
    }

    override fun logException(t: Throwable) {
        loggedExceptions += t
    }

    override fun setAnalyticsConsent(consents: Boolean?) {
        hasConsent = consents
    }

    fun reset() {
        currentUserID = null
        hasConsent = null
        loggedErrors.clear()
        loggedExceptions.clear()
    }
}

class FakeCrashReporter : CrashReporter {
    val customKeys = mutableMapOf<String, String>()
    val recordedExceptions = mutableListOf<Throwable>()

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        customKeys[key] = value
    }

    override fun recordException(throwable: Throwable) {
        recordedExceptions += throwable
    }

    fun reset() {
        customKeys.clear()
        recordedExceptions.clear()
    }
}

class NoOpPerformanceMonitor : PerformanceMonitor {
    override fun newTrace(name: String): PerformanceTrace =
        object : PerformanceTrace {
            override fun start() = Unit

            override fun stop() = Unit
        }
}
