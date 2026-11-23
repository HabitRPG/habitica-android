package com.habitrpg.android.habitica.extensions

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment

fun Fragment.openBrowserLink(url: String) {
    val uriUrl = url.toUri()
    val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
    try {
        startActivity(launchBrowser)
    } catch (_: ActivityNotFoundException) {
        // Links can come from the remote config, so a broken one must not crash the app
    }
}
