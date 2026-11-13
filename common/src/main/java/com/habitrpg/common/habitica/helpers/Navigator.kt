package com.habitrpg.common.habitica.helpers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections

interface Navigator {
    fun navigate(
        transactionId: Int,
        args: Bundle? = null,
    )

    fun navigate(directions: NavDirections)

    fun navigate(uriString: String)

    fun navigate(uri: Uri)

    fun navigate(request: NavDeepLinkRequest)

    fun handle(deeplink: Intent)

    fun navigateBack()
}
