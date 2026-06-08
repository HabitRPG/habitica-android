package com.habitrpg.android.habitica.widget.glance.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.currentState
import androidx.glance.layout.ContentScale
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.activities.AddTaskWidgetActivity
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.actions.openTaskFormAction
import com.habitrpg.android.habitica.widget.glance.components.stringRes
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme

class AddTaskSingleGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isLoggedIn = WidgetAuth.isLoggedIn(context)
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val configureIntent = Intent(context, AddTaskWidgetActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val configureAction = if (isLoggedIn) actionStartActivity(configureIntent) else openAppAction()
        provideContent {
            val type = currentState<Preferences>()[stringPreferencesKey(TASK_TYPE_KEY)]
            Log.d("AddTaskWidget", "provideGlance widgetId=$widgetId type=$type loggedIn=$isLoggedIn")
            HabiticaWidgetTheme {
                AddTaskSingleContent(
                    type = type,
                    onConfigure = configureAction,
                    isLoggedIn = isLoggedIn,
                )
            }
        }
    }

    companion object {
        const val TASK_TYPE_KEY = "task_type"
    }
}

private data class TileSpec(
    val nameResId: Int,
    val iconResId: Int,
    val taskType: String,
)

private fun tileFor(type: String?): TileSpec? = when (type) {
    "habit" -> TileSpec(R.string.habit, R.drawable.widget_add_habit_glyph, "habit")
    "daily" -> TileSpec(R.string.daily, R.drawable.widget_add_daily_glyph, "daily")
    "todo" -> TileSpec(R.string.widget_add_task_todo_compact, R.drawable.widget_add_todo_glyph, "todo")
    "reward" -> TileSpec(R.string.reward, R.drawable.widget_add_reward_glyph, "reward")
    else -> null
}

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
private fun AddTaskSingleContent(type: String?, onConfigure: Action, isLoggedIn: Boolean) {
    val tile = tileFor(type)
    if (tile == null) {
        UnsetTaskTypeContent(onClick = onConfigure)
        return
    }
    val tileAction = if (isLoggedIn) openTaskFormAction(tile.taskType) else openAppAction()
    val size = LocalSize.current
    val scallopSide = minOf(size.width, size.height)
    val iconSize = (scallopSide.value * 0.55f).coerceIn(28f, 240f).dp

    val context = LocalContext.current
    val scallopTint: ColorProvider
    val baseIconTint: ColorProvider
    if (MaterialYouEnabled) {
        scallopTint = GlanceTheme.colors.widgetBackground
        baseIconTint = GlanceTheme.colors.primary
    } else {
        scallopTint = ColorProvider(R.color.widget_bg)
        baseIconTint = ColorProvider(R.color.widget_dailies_purple)
    }
    val iconTint = ColorProvider(baseIconTint.getColor(context).copy(alpha = 0.9f))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = tileAction),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier.size(scallopSide),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_tile_scallop),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(scallopTint),
            )
            Image(
                provider = ImageProvider(tile.iconResId),
                contentDescription = stringRes(R.string.widget_add_new_task_cd, stringRes(tile.nameResId)),
                modifier = GlanceModifier.size(iconSize),
                colorFilter = ColorFilter.tint(iconTint),
            )
        }
    }
}

@Composable
private fun UnsetTaskTypeContent(onClick: Action) {
    val widgetBackground: ColorProvider = if (MaterialYouEnabled) {
        GlanceTheme.colors.surfaceVariant
    } else {
        ColorProvider(R.color.widget_bg)
    }
    val textColor: ColorProvider = if (MaterialYouEnabled) {
        GlanceTheme.colors.onSurfaceVariant
    } else {
        ColorProvider(R.color.widget_text)
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_tile_scallop),
            contentDescription = null,
            modifier = GlanceModifier.size(47.dp),
            colorFilter = ColorFilter.tint(widgetBackground),
        )
        Text(
            text = stringRes(R.string.widget_unconfigured_choose_type),
            style = TextStyle(
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
