package com.habitrpg.android.habitica.helpers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import java.lang.ref.WeakReference
import java.util.Date
import kotlin.math.abs

object MainNavigationController {
    var lastNavigation: Date? = null

    private var controllerReference: WeakReference<NavController>? = null

    private val navController: NavController?
        get() { return controllerReference?.get() }

    val isReady: Boolean
        get() = controllerReference?.get() != null

    fun setup(navController: NavController) {
        this.controllerReference = WeakReference(navController)
    }

    fun updateLabel(destinationID: Int, label: String) {
        navController?.findDestination(destinationID)?.label = label
    }

    fun navigate(transactionId: Int, args: Bundle? = null) {
        if (abs((lastNavigation?.time ?: 0) - Date().time) > 500) {
            lastNavigation = Date()
            try {
                navController?.navigate(transactionId, args)
            } catch (e: IllegalArgumentException) {
                Log.e("Main Navigation", e.localizedMessage ?: "")
            } catch (error: Exception) {
                Log.e("Main Navigation", error.localizedMessage ?: "")
            }
        }
    }

    fun navigate(directions: NavDirections) {
        if (abs((lastNavigation?.time ?: 0) - Date().time) > 500) {
            lastNavigation = Date()
            try {
                navController?.navigate(directions)
            } catch (_: IllegalArgumentException) {}
        }
    }

    fun navigate(uriString: String) {
        val uri = Uri.parse(uriString)
        var builder = uri.buildUpon()
        if (uri.scheme == null) {
            builder = builder.scheme("https")
        }
        if (uri.host == null) {
            builder = builder.authority("habitica.com")
        }
        navigate(builder.build())
    }

    fun navigate(uri: Uri) {
        if (navController?.graph?.hasDeepLink(uri) == true) {
            navController?.navigate(uri)
        }
    }

    fun navigate(request: NavDeepLinkRequest) {
        if (navController?.graph?.hasDeepLink(request) == true) {
            navController?.navigate(request)
        }
    }

    fun handle(deeplink: Intent) {
        navController?.handleDeepLink(deeplink)
    }

    fun navigateBack() {
        navController?.navigateUp()
    }
}
