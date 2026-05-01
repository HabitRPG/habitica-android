package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.RunCronAction
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.DailyCountGauge
import com.habitrpg.android.habitica.widget.glance.components.ProgressBar
import com.habitrpg.android.habitica.widget.glance.data.DailyCountWidgetState
import com.habitrpg.android.habitica.widget.glance.data.computeNeedsCron
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.flow.firstOrNull

class DailiesCountGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = widgetEntryPoint(context)
        val user = entry.userRepository().getUser().firstOrNull()
        val mirroredGroupIds = user?.preferences?.tasks?.mirrorGroupTasks
            ?.toTypedArray() ?: emptyArray()
        val tasks = entry.taskRepository().getTasks(
            taskType = TaskType.DAILY,
            userID = user?.id,
            includedGroupIDs = mirroredGroupIds,
        ).firstOrNull().orEmpty().filter { it.isDue == true }

        val state = DailyCountWidgetState(
            totalDue = tasks.size,
            completed = tasks.count { it.completed },
            needsCron = computeNeedsCron(user),
        )

        val prefs = getAppWidgetState<Preferences>(context, id)
        val showRemaining = prefs[WidgetStateKeys.dailiesCountShowRemaining] ?: false

        provideContent {
            HabiticaWidgetTheme {
                DailiesCountContent(state, showRemaining)
            }
        }
    }
}

@Composable
private fun DailiesCountContent(state: DailyCountWidgetState, showRemaining: Boolean) {
    val isAllDone = state.totalDue > 0 && state.completed == state.totalDue

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background),
    ) {
        when {
            state.needsCron -> StartDayContent()
            isAllDone -> AllDoneContent(state.totalDue)
            else -> DailyCountGauge(
                completedCount = state.completed,
                totalCount = state.totalDue,
                showRemaining = showRemaining,
                onClick = openAppAction("habitica://user/tasks/daily"),
            )
        }
    }
}

@Composable
private fun StartDayContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(onClick = actionRunCallback<RunCronAction>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_start_day),
            contentDescription = "Start a new day",
            modifier = GlanceModifier.size(36.dp),
        )
        Spacer(GlanceModifier.height(12.dp))
        Text(
            text = "Start a new day",
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun AllDoneContent(totalCount: Int) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(onClick = openAppAction("habitica://user/tasks/daily")),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = totalCount.toString(),
                style = TextStyle(
                    color = WidgetColors.dailiesPurple,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(GlanceModifier.width(4.dp))
            Image(
                provider = ImageProvider(R.drawable.widget_sparkles),
                contentDescription = null,
                modifier = GlanceModifier.size(28.dp),
            )
        }
        Text(
            text = "Dailies done",
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.height(10.dp))
        ProgressBar(
            fillColor = ColorProvider(WidgetBarColors.purple),
            trackColor = WidgetColors.progressTrack,
            progress = 1f,
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "All done today!",
            style = TextStyle(
                color = WidgetColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}
