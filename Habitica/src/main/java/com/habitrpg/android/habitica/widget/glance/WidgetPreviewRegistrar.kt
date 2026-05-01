package com.habitrpg.android.habitica.widget.glance

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import androidx.collection.intSetOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.widget.AddTaskMultiWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetPreviewRegistrar {

    private const val PREFS = "widget_previews"
    private const val LAST_VERSION_KEY = "last_registered_version"

    fun registerIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(LAST_VERSION_KEY, -1) == BuildConfig.VERSION_CODE) return

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching {
                GlanceAppWidgetManager(context).setWidgetPreviews(
                    receiver = AddTaskMultiWidgetReceiver::class,
                    widgetCategories = intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN),
                )
                prefs.edit().putInt(LAST_VERSION_KEY, BuildConfig.VERSION_CODE).apply()
            }
        }
    }
}
