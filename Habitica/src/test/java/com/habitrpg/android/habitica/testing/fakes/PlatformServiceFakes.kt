package com.habitrpg.android.habitica.testing.fakes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import com.habitrpg.android.habitica.helpers.CrashReporter
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

class FakeCrashReporter : CrashReporter {
    val customKeys = mutableMapOf<String, String>()
    val recordedExceptions = mutableListOf<Throwable>()

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        customKeys[key] = value
    }

    override fun logException(throwable: Throwable) {
        recordedExceptions += throwable
    }

    override fun setUserID(userID: String) = Unit

    override fun logError(msg: String) = Unit

    override fun setAnalyticsConsent(consents: Boolean?) = Unit

    override fun initialize(context: Context) = Unit

    override fun clearUserID() = Unit

    fun reset() {
        customKeys.clear()
        recordedExceptions.clear()
    }
}
