package com.habitrpg.android.habitica.helpers

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import java.lang.ref.WeakReference
import java.util.*

object MainNavigationController {
    var lastNavigation: Date? = null

    var navController: WeakReference<NavController>? = null

    fun setup(navController: NavController) {
        this.navController = WeakReference(navController)
    }

    fun navigate(transactionId: Int, args: Bundle? = null) {
        if (Math.abs((lastNavigation?.time ?: 0) - Date().time) > 500) {
            lastNavigation = Date()
            navController?.get()?.navigate(transactionId, args)
        }
    }

    fun navigate(directions: NavDirections) {
        if (Math.abs((lastNavigation?.time ?: 0) - Date().time) > 500) {
            lastNavigation = Date()
            navController?.get()?.navigate(directions)
        }
    }
}