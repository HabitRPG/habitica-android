package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Intent
import android.net.Uri
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity

fun openAppAction(deepLink: String? = null): Action {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setClassName("com.habitrpg.android.habitica", MainActivity::class.java.name)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    if (!deepLink.isNullOrEmpty()) {
        intent.data = Uri.parse(deepLink)
    }
    return actionStartActivity(intent)
}
