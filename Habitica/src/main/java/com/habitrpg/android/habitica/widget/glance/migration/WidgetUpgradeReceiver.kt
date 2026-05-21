package com.habitrpg.android.habitica.widget.glance.migration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.habitrpg.android.habitica.widget.glance.work.WidgetRefreshWorker
import com.habitrpg.android.habitica.widget.glance.work.WidgetUpgradeWorker

class WidgetUpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        WidgetRefreshWorker.enqueue(context)
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<WidgetUpgradeWorker>().build(),
        )
    }
}
