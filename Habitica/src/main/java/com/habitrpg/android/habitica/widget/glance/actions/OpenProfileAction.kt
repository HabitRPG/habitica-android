package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity

fun openProfileAction(userId: String?): Action {
    if (userId.isNullOrEmpty()) return openAppAction()
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setClassName("com.habitrpg.android.habitica", FullProfileActivity::class.java.name)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.putExtra("userID", userId)
    return actionStartActivity(intent)
}
