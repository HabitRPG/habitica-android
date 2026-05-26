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
import androidx.datastore.preferences.core.Preferences
import androidx.glance.currentState
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
import com.habitrpg.android.habitica.widget.glance.actions.openTaskFormAction
import com.habitrpg.android.habitica.widget.glance.components.EmptyState
import com.habitrpg.android.habitica.widget.glance.components.StartDayCard
import com.habitrpg.android.habitica.widget.glance.components.TaskRow
import com.habitrpg.android.habitica.widget.glance.components.stringRes
import com.habitrpg.android.habitica.widget.glance.data.TaskListMemoryCache
import com.habitrpg.android.habitica.widget.glance.data.TaskListWidgetState
import com.habitrpg.android.habitica.widget.glance.data.computeNeedsCron
import com.habitrpg.android.habitica.widget.glance.data.toWidgetItem
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import com.habitrpg.android.habitica.widget.glance.theme.colorForTaskValueLight
import com.habitrpg.android.habitica.widget.glance.theme.colorForTaskValueMedium
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

abstract class TaskListGlanceWidget(
    private val taskType: TaskType,
) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 120.dp),
            DpSize(220.dp, 160.dp),
            DpSize(300.dp, 200.dp),
            DpSize(360.dp, 300.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = TaskListMemoryCache.get(taskType) ?: withContext(Dispatchers.Main) {
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
            val fresh = TaskListWidgetState(
                tasks = visible.map { it.toWidgetItem() },
                needsCron = computeNeedsCron(user),
            )
            TaskListMemoryCache.put(taskType, fresh)
            fresh
        }

        provideContent {
            val hiddenIds = currentState<Preferences>()[WidgetStateKeys.taskListHiddenIds] ?: emptySet()
            val filtered = if (hiddenIds.isEmpty()) state else state.copy(
                tasks = state.tasks.filterNot { it.id in hiddenIds },
            )
            HabiticaWidgetTheme {
                TaskListContent(filtered, isDaily = taskType == TaskType.DAILY)
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
    val cardIconTint: ColorProvider?,
    val checklistChipBackground: ColorProvider,
    val checklistChipBackgroundDone: ColorProvider,
    val checklistChipText: ColorProvider,
    val checklistChipTextDone: ColorProvider,
)

@Composable
private fun rememberPalette(): TaskListPalette {
    return if (MaterialYouEnabled) {
        TaskListPalette(
            widgetBackground = GlanceTheme.colors.primaryContainer,
            cardBackground = GlanceTheme.colors.secondaryContainer,
            titleText = GlanceTheme.colors.onPrimaryContainer,
            taskText = GlanceTheme.colors.onSecondaryContainer,
            secondaryText = GlanceTheme.colors.onSurfaceVariant,
            iconTint = GlanceTheme.colors.onPrimaryContainer,
            cardIconTint = GlanceTheme.colors.onSecondaryContainer,
            checklistChipBackground = GlanceTheme.colors.tertiaryContainer,
            checklistChipBackgroundDone = GlanceTheme.colors.surfaceVariant,
            checklistChipText = GlanceTheme.colors.onTertiaryContainer,
            checklistChipTextDone = GlanceTheme.colors.onSurfaceVariant,
        )
    } else {
        TaskListPalette(
            widgetBackground = WidgetColors.background,
            cardBackground = WidgetColors.cardBackground,
            titleText = WidgetColors.taskListPrimaryText,
            taskText = WidgetColors.taskListTaskText,
            secondaryText = WidgetColors.textSecondary,
            iconTint = ColorProvider(R.color.widget_task_list_add_icon),
            cardIconTint = null,
            checklistChipBackground = WidgetColors.checklistBackground,
            checklistChipBackgroundDone = WidgetColors.checklistBackgroundDone,
            checklistChipText = ColorProvider(androidx.compose.ui.graphics.Color.White),
            checklistChipTextDone = WidgetColors.textSecondary,
        )
    }
}

@Composable
private fun TaskListContent(state: TaskListWidgetState, isDaily: Boolean) {
    val size = LocalSize.current
    val palette = rememberPalette()
    val isVeryCompact = size.width < 180.dp
    val isCompact = size.width < 230.dp
    val openListLink = if (isDaily) "habitica://user/tasks/daily" else "habitica://user/tasks/todo"
    val addTaskType = if (isDaily) "daily" else "todo"
    val title = when {
        !isDaily && isCompact -> stringRes(R.string.todos)
        !isDaily -> stringRes(R.string.widget_list_title_todos_full)
        isCompact -> stringRes(R.string.dailies)
        else -> stringRes(R.string.widget_list_title_dailies_full)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(palette.widgetBackground)
            .padding(
                horizontal = if (isVeryCompact) 6.dp else 10.dp,
                vertical = if (isVeryCompact) 8.dp else 12.dp,
            ),
    ) {
        TaskListHeader(
            title = title,
            palette = palette,
            openListLink = openListLink,
            addTaskType = addTaskType,
            isVeryCompact = isVeryCompact,
        )
        Spacer(GlanceModifier.height(if (isVeryCompact) 8.dp else 14.dp))
        TaskListBody(
            state = state,
            isDaily = isDaily,
            palette = palette,
        )
    }
}

@Composable
private fun TaskListHeader(
    title: String,
    palette: TaskListPalette,
    openListLink: String,
    addTaskType: String,
    isVeryCompact: Boolean,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = if (isVeryCompact) 6.dp else 14.dp, end = if (isVeryCompact) 6.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = palette.titleText,
                fontSize = if (isVeryCompact) 15.sp else 22.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(onClick = openAppAction(openListLink)),
        )
        if (!isVeryCompact) {
            Image(
                provider = ImageProvider(R.drawable.widget_icon_add),
                contentDescription = stringRes(R.string.widget_add_task_cd),
                modifier = GlanceModifier
                    .size(20.dp)
                    .clickable(onClick = openTaskFormAction(addTaskType)),
                colorFilter = palette.iconTint?.let { ColorFilter.tint(it) },
            )
        }
    }
}

@Composable
private fun TaskListBody(
    state: TaskListWidgetState,
    isDaily: Boolean,
    palette: TaskListPalette,
) {
    Box(modifier = GlanceModifier.fillMaxSize()) {
        when {
            state.needsCron && isDaily -> StartDayCard(
                onClick = actionRunCallback<RunCronAction>(),
                backgroundColor = palette.cardBackground,
                textColor = palette.taskText,
                iconTint = palette.cardIconTint,
            )
            state.tasks.isEmpty() -> EmptyState(
                message = stringRes(
                    if (isDaily) R.string.widget_empty_dailies else R.string.widget_empty_todos,
                ),
                backgroundColor = palette.cardBackground,
                textColor = palette.taskText,
            )
            else -> TaskListRows(state = state, palette = palette, isDaily = isDaily)
        }
    }
}

@Composable
private fun TaskListRows(
    state: TaskListWidgetState,
    palette: TaskListPalette,
    isDaily: Boolean,
) {
    val innerCornerRadius = if (isDaily) 8.dp else 13.dp
    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
        items(state.tasks.size, itemId = { state.tasks[it].id.hashCode().toLong() }) { index ->
            val task = state.tasks[index]
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
                        checklistChipBackground = palette.checklistChipBackground,
                        checklistChipBackgroundDone = palette.checklistChipBackgroundDone,
                        checklistChipText = palette.checklistChipText,
                        checklistChipTextDone = palette.checklistChipTextDone,
                        checklistDoneCount = task.checklistDone,
                        checklistTotalCount = task.checklistTotal,
                        showChecklistCount = true,
                        innerCornerRadius = innerCornerRadius,
                        onClick = actionRunCallback<ScoreTaskAction>(
                            actionParametersOf(
                                WidgetActionKeys.taskId to task.id,
                                WidgetActionKeys.direction to TaskDirection.UP.text,
                            ),
                        ),
                    )
                }
                if (index < state.tasks.size - 1) {
                    Spacer(GlanceModifier.height(6.dp))
                }
            }
        }
    }
}
