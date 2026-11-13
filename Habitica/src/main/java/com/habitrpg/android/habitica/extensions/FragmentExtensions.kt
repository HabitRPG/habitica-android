package com.habitrpg.android.habitica.extensions

import android.content.Intent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment

fun Fragment.openBrowserLink(url: String) {
    val uriUrl = url.toUri()
    val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
    startActivity(launchBrowser)
}
