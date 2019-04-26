package com.habitrpg.android.habitica.helpers

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import java.lang.ref.WeakReference

object MainNavigationController {

    var navController: WeakReference<NavController>? = null

    fun setup(navController: NavController) {
        this.navController = WeakReference(navController)
    }

    fun navigate(transactionId: Int, args: Bundle? = null) {
        navController?.get()?.navigate(transactionId, args)
    }

    fun navigate(directions: NavDirections) {
        navController?.get()?.navigate(directions)
    }
}