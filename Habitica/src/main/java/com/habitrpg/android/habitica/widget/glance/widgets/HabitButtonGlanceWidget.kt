package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.widget.glance.actions.ScoreHabitAction
import com.habitrpg.android.habitica.widget.glance.components.HabitButtonsRow
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.colorForTaskValue
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import kotlinx.coroutines.flow.firstOrNull

class HabitButtonGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val taskId = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("habit_button_widget_$widgetId", null)

        val task = if (taskId.isNullOrEmpty()) null else
            widgetEntryPoint(context).taskRepository().getTask(taskId).firstOrNull()

        provideContent {
            HabiticaWidgetTheme {
                if (task == null) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(GlanceTheme.colors.background)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Tap to configure habit",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                } else {
                    val color = colorForTaskValue(task.value)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(GlanceTheme.colors.background),
                    ) {
                        HabitButtonsRow(
                            text = task.text,
                            showUp = task.up == true,
                            showDown = task.down == true,
                            upColor = color,
                            downColor = color,
                            textColor = GlanceTheme.colors.onBackground,
                            onUpClick = actionRunCallback<ScoreHabitAction>(
                                actionParametersOf(
                                    WidgetActionKeys.taskId to task.id.orEmpty(),
                                    WidgetActionKeys.direction to TaskDirection.UP.text,
                                ),
                            ),
                            onDownClick = actionRunCallback<ScoreHabitAction>(
                                actionParametersOf(
                                    WidgetActionKeys.taskId to task.id.orEmpty(),
                                    WidgetActionKeys.direction to TaskDirection.DOWN.text,
                                ),
                            ),
                        )
                    }
                }
            }
        }
    }
}
