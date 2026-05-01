package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
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
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.theme.AddTaskTileColors
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme

class AddTaskSingleGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val type = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("add_task_widget_$widgetId", null)
        provideContent {
            HabiticaWidgetTheme {
                AddTaskSingleContent(type)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            HabiticaWidgetTheme {
                AddTaskSingleContent("habit")
            }
        }
    }
}

private data class TileSpec(
    val name: String,
    val brandColor: Color,
    val iconResId: Int,
    val deepLink: String,
)

private fun tileFor(type: String?): TileSpec? = when (type) {
    "habit" -> TileSpec("Habit", AddTaskTileColors.habit, R.drawable.widget_add_habit_glyph, "habitica://user/tasks/habit/add")
    "daily" -> TileSpec("Daily", AddTaskTileColors.daily, R.drawable.widget_add_daily_glyph, "habitica://user/tasks/daily/add")
    "todo" -> TileSpec("Todo", AddTaskTileColors.todo, R.drawable.widget_add_todo_glyph, "habitica://user/tasks/todo/add")
    "reward" -> TileSpec("Reward", AddTaskTileColors.reward, R.drawable.widget_add_reward_glyph, "habitica://user/tasks/reward/add")
    else -> null
}

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
private fun AddTaskSingleContent(type: String?) {
    val tile = tileFor(type)
    if (tile == null) {
        UnsetTaskTypeContent()
        return
    }
    val size = LocalSize.current
    val shorter = if (size.width < size.height) size.width else size.height
    val iconSize = (shorter.value * 0.30f).coerceIn(20f, 72f).dp
    val tilePadding = (shorter.value * 0.06f).coerceIn(2f, 12f).dp

    val tileColor: ColorProvider
    val iconTint: ColorProvider?
    if (MaterialYouEnabled) {
        tileColor = GlanceTheme.colors.primaryContainer
        iconTint = GlanceTheme.colors.onPrimaryContainer
    } else {
        tileColor = ColorProvider(tile.brandColor)
        iconTint = null
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(tilePadding)
            .clickable(onClick = openAppAction(tile.deepLink)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_tile_scallop),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(tileColor),
        )
        Image(
            provider = ImageProvider(tile.iconResId),
            contentDescription = "Add new ${tile.name}",
            modifier = GlanceModifier.size(iconSize),
            colorFilter = iconTint?.let { ColorFilter.tint(it) },
        )
    }
}

@Composable
private fun UnsetTaskTypeContent() {
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
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_tile_scallop),
            contentDescription = null,
            modifier = GlanceModifier.size(56.dp),
            colorFilter = ColorFilter.tint(widgetBackground),
        )
        Text(
            text = "Tap to choose task type",
            style = TextStyle(
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
