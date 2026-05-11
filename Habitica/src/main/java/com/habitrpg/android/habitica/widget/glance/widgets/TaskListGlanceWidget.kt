package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.RunCronAction
import com.habitrpg.android.habitica.widget.glance.actions.ScoreTaskAction
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.EmptyState
import com.habitrpg.android.habitica.widget.glance.components.StartDayCard
import com.habitrpg.android.habitica.widget.glance.components.TaskRow
import com.habitrpg.android.habitica.widget.glance.data.TaskListWidgetState
import com.habitrpg.android.habitica.widget.glance.data.computeNeedsCron
import com.habitrpg.android.habitica.widget.glance.data.toWidgetItem
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import com.habitrpg.android.habitica.widget.glance.theme.colorForTaskValueLight
import com.habitrpg.android.habitica.widget.glance.theme.colorForTaskValueMedium
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.flow.firstOrNull

abstract class TaskListGlanceWidget(
    private val taskType: TaskType,
) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(220.dp, 160.dp),
            DpSize(300.dp, 200.dp),
            DpSize(360.dp, 300.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = widgetEntryPoint(context)
        val user = entry.userRepository().getUser().firstOrNull()
        val mirroredGroupIds = user?.preferences?.tasks?.mirrorGroupTasks
            ?.toTypedArray() ?: emptyArray()
        val raw = entry.taskRepository().getTasks(
            taskType = taskType,
            userID = user?.id,
            includedGroupIDs = mirroredGroupIds,
        ).firstOrNull().orEmpty()
        val visible = raw.filter {
            !it.completed && (taskType != TaskType.DAILY || it.isDue == true)
        }
        val state = TaskListWidgetState(
            tasks = visible.map { it.toWidgetItem() },
            needsCron = computeNeedsCron(user),
        )

        provideContent {
            HabiticaWidgetTheme {
                TaskListContent(state, isDaily = taskType == TaskType.DAILY)
            }
        }
    }
}

class DailyTaskListGlanceWidget : TaskListGlanceWidget(TaskType.DAILY)
class TodoTaskListGlanceWidget : TaskListGlanceWidget(TaskType.TODO)

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

internal data class TaskListPalette(
    val widgetBackground: ColorProvider,
    val cardBackground: ColorProvider,
    val titleText: ColorProvider,
    val taskText: ColorProvider,
    val secondaryText: ColorProvider,
    val iconTint: ColorProvider?,
)

@Composable
private fun rememberPalette(): TaskListPalette {
    return if (MaterialYouEnabled) {
        TaskListPalette(
            widgetBackground = GlanceTheme.colors.secondaryContainer,
            cardBackground = GlanceTheme.colors.background,
            titleText = GlanceTheme.colors.onSecondaryContainer,
            taskText = GlanceTheme.colors.onSecondaryContainer,
            secondaryText = GlanceTheme.colors.onSurfaceVariant,
            iconTint = GlanceTheme.colors.onSecondaryContainer,
        )
    } else {
        TaskListPalette(
            widgetBackground = WidgetColors.background,
            cardBackground = WidgetColors.cardBackground,
            titleText = WidgetColors.taskListPrimaryText,
            taskText = WidgetColors.taskListTaskText,
            secondaryText = WidgetColors.textSecondary,
            iconTint = null,
        )
    }
}

@Composable
private fun TaskListContent(state: TaskListWidgetState, isDaily: Boolean) {
    val size = LocalSize.current
    val palette = rememberPalette()
    val isCompact = size.width < 230.dp
    val isLarge = size.height >= 280.dp
    val openListLink = if (isDaily) "habitica://user/tasks/daily" else "habitica://user/tasks/todo"
    val addLink = if (isDaily) "habitica://user/tasks/daily/add" else "habitica://user/tasks/todo/add"
    val title = when {
        !isDaily && isCompact -> "To Do's"
        !isDaily -> "Your To Do's"
        isCompact -> "Dailies"
        else -> "Today's Dailies"
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(palette.widgetBackground)
            .padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        TaskListHeader(
            title = title,
            palette = palette,
            openListLink = openListLink,
            addLink = addLink,
        )
        Spacer(GlanceModifier.height(8.dp))
        TaskListBody(
            state = state,
            isDaily = isDaily,
            isLarge = isLarge,
            palette = palette,
            openListLink = openListLink,
        )
    }
}

@Composable
private fun TaskListHeader(
    title: String,
    palette: TaskListPalette,
    openListLink: String,
    addLink: String,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = palette.titleText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(onClick = openAppAction(openListLink)),
        )
        Image(
            provider = ImageProvider(R.drawable.widget_icon_add),
            contentDescription = "Add task",
            modifier = GlanceModifier
                .size(20.dp)
                .clickable(onClick = openAppAction(addLink)),
            colorFilter = palette.iconTint?.let { ColorFilter.tint(it) },
        )
    }
}

@Composable
private fun TaskListBody(
    state: TaskListWidgetState,
    isDaily: Boolean,
    isLarge: Boolean,
    palette: TaskListPalette,
    openListLink: String,
) {
    Box(modifier = GlanceModifier.fillMaxSize()) {
        when {
            state.needsCron && isDaily -> StartDayCard(
                onClick = actionRunCallback<RunCronAction>(),
                backgroundColor = palette.cardBackground,
                textColor = palette.titleText,
                iconTint = palette.iconTint,
            )
            state.tasks.isEmpty() -> EmptyState(
                message = if (isDaily) "All done today!" else "All done!",
                backgroundColor = palette.cardBackground,
                textColor = palette.titleText,
            )
            else -> TaskListRows(
                state = state,
                isDaily = isDaily,
                isLarge = isLarge,
                palette = palette,
                openListLink = openListLink,
            )
        }
    }
}

@Composable
private fun TaskListRows(
    state: TaskListWidgetState,
    isDaily: Boolean,
    isLarge: Boolean,
    palette: TaskListPalette,
    openListLink: String,
) {
    val maxVisible = if (isLarge) 9 else 6
    val visible = state.tasks.take(maxVisible)
    val remaining = state.tasks.size - visible.size

    Column(modifier = GlanceModifier.fillMaxSize()) {
        LazyColumn(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
            items(visible.size, itemId = { visible[it].id.hashCode().toLong() }) { index ->
                val task = visible[index]
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .cornerRadius(17.5.dp)
                            .background(palette.cardBackground),
                    ) {
                        TaskRow(
                            text = task.text,
                            valueColor = colorForTaskValueLight(task.value),
                            valueBorderColor = colorForTaskValueMedium(task.value),
                            primaryTextColor = palette.taskText,
                            checklistDoneCount = task.checklistDone,
                            checklistTotalCount = task.checklistTotal,
                            showChecklistCount = isLarge,
                            onClick = actionRunCallback<ScoreTaskAction>(
                                actionParametersOf(
                                    WidgetActionKeys.taskId to task.id,
                                    WidgetActionKeys.direction to TaskDirection.UP.text,
                                ),
                            ),
                        )
                    }
                    if (index < visible.size - 1 || (isLarge && remaining > 0)) {
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        }
        if (isLarge && remaining > 0) {
            val plural = if (isDaily) {
                if (remaining == 1) "1 more unfinished Daily" else "$remaining more unfinished Dailies"
            } else {
                if (remaining == 1) "1 more unfinished To Do" else "$remaining more unfinished To Do's"
            }
            Text(
                text = plural,
                style = TextStyle(
                    color = palette.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = GlanceModifier
                    .padding(start = 12.dp, top = 6.dp)
                    .clickable(onClick = openAppAction(openListLink)),
            )
        }
    }
}
