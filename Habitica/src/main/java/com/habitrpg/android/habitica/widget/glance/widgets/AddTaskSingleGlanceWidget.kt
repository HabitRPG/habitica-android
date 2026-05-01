package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
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
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

class AddTaskSingleGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

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
}

private data class TileSpec(
    val name: String,
    val color: Color,
    val iconResId: Int,
    val deepLink: String,
)

private fun tileFor(type: String?): TileSpec? = when (type) {
    "habit" -> TileSpec("Habit", AddTaskTileColors.habit, R.drawable.widget_add_habit_icon, "habitica://user/tasks/habit/add")
    "daily" -> TileSpec("Daily", AddTaskTileColors.daily, R.drawable.widget_add_daily_icon, "habitica://user/tasks/daily/add")
    "todo" -> TileSpec("Todo", AddTaskTileColors.todo, R.drawable.widget_add_todo_icon, "habitica://user/tasks/todo/add")
    "reward" -> TileSpec("Reward", AddTaskTileColors.reward, R.drawable.widget_add_reward_icon, "habitica://user/tasks/reward/add")
    else -> null
}

@Composable
private fun AddTaskSingleContent(type: String?) {
    val tile = tileFor(type)
    if (tile == null) {
        UnsetTaskTypeContent()
        return
    }
    val displayName = if (tile.name == "Todo") "To Do" else tile.name
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(tile.color))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(onClick = openAppAction(tile.deepLink)),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Image(
            provider = ImageProvider(tile.iconResId),
            contentDescription = displayName,
            modifier = GlanceModifier.size(48.dp),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "Add new\n$displayName",
            style = TextStyle(
                color = WidgetColors.addLabelText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun UnsetTaskTypeContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_edit_task_type),
            contentDescription = null,
            modifier = GlanceModifier.size(36.dp),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = "Edit to select a task type",
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
