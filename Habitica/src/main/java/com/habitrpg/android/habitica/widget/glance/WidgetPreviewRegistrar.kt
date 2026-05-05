package com.habitrpg.android.habitica.widget.glance

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import androidx.collection.intSetOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.widget.AddTaskMultiWidgetReceiver
import com.habitrpg.android.habitica.widget.AddTaskWidgetProvider
import com.habitrpg.android.habitica.widget.DailiesCountWidgetReceiver
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.android.habitica.widget.TodoListWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

object WidgetPreviewRegistrar {

    private const val PREFS = "widget_previews"
    private const val LAST_KEY = "last_registered_token"

    private const val REVISION = 9

    private val RECEIVERS: List<KClass<out GlanceAppWidgetReceiver>> = listOf(
        AddTaskMultiWidgetReceiver::class,
        AddTaskWidgetProvider::class,
        DailiesCountWidgetReceiver::class,
        DailiesWidgetProvider::class,
        HabitButtonWidgetProvider::class,
        TodoListWidgetProvider::class,
    )

    fun registerIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        val token = "${BuildConfig.VERSION_CODE}.$REVISION"
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(LAST_KEY, null) == token) return

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val categories = intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
            val manager = GlanceAppWidgetManager(context)
            val allOk = RECEIVERS.all { receiver ->
                runCatching { manager.setWidgetPreviews(receiver, categories) }.isSuccess
            }
            if (allOk) {
                prefs.edit().putString(LAST_KEY, token).apply()
            }
        }
    }
}
