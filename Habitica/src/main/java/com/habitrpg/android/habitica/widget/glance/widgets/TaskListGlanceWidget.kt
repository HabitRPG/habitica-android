package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.RunCronAction
import com.habitrpg.android.habitica.widget.glance.actions.ScoreTaskAction
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.EmptyState
import com.habitrpg.android.habitica.widget.glance.components.StartDayCard
import com.habitrpg.android.habitica.widget.glance.components.TaskRow
import com.habitrpg.android.habitica.widget.glance.components.TaskRowSeparator
import com.habitrpg.android.habitica.widget.glance.data.TaskListWidgetState
import com.habitrpg.android.habitica.widget.glance.data.computeNeedsCron
import com.habitrpg.android.habitica.widget.glance.data.toWidgetItem
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import com.habitrpg.android.habitica.widget.glance.theme.colorForTaskValue
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

@Composable
private fun TaskListContent(state: TaskListWidgetState, isDaily: Boolean) {
    val size = LocalSize.current
    val isSidebarLayout = size.width < 300.dp
    val isLarge = size.height >= 280.dp
    val openListLink = if (isDaily) "habitica://user/tasks/daily" else "habitica://user/tasks/todo"
    val addLink = if (isDaily) "habitica://user/tasks/daily/add" else "habitica://user/tasks/todo/add"

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .padding(12.dp),
    ) {
        if (isSidebarLayout) {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                TaskListSidebar(
                    title = if (isDaily) "Dailies" else "To Do's",
                    count = state.tasks.size,
                    openListLink = openListLink,
                    addLink = addLink,
                )
                Spacer(GlanceModifier.width(12.dp))
                TaskListBody(
                    state = state,
                    isDaily = isDaily,
                    isLarge = false,
                    openListLink = openListLink,
                )
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                TaskListHeader(
                    title = if (isDaily) "Today's Dailies" else "Your To Do's",
                    openListLink = openListLink,
                    addLink = addLink,
                )
                Spacer(GlanceModifier.height(2.dp))
                TaskListBody(
                    state = state,
                    isDaily = isDaily,
                    isLarge = isLarge,
                    openListLink = openListLink,
                )
            }
        }
    }
}

@Composable
private fun TaskListSidebar(
    title: String,
    count: Int,
    openListLink: String,
    addLink: String,
) {
    Column(
        modifier = GlanceModifier
            .width(60.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = WidgetColors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier
                .padding(top = 4.dp)
                .clickable(onClick = openAppAction(openListLink)),
        )
        Text(
            text = count.toString(),
            style = TextStyle(
                color = WidgetColors.taskListSecondaryText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
            ),
            modifier = GlanceModifier.clickable(onClick = openAppAction(openListLink)),
        )
        Spacer(GlanceModifier.defaultWeight())
        Image(
            provider = ImageProvider(R.drawable.widget_icon_add),
            contentDescription = "Add task",
            modifier = GlanceModifier
                .size(28.dp)
                .padding(bottom = 7.dp)
                .clickable(onClick = openAppAction(addLink)),
        )
    }
}

@Composable
private fun TaskListHeader(title: String, openListLink: String, addLink: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = WidgetColors.taskListPrimaryText,
                fontSize = 20.sp,
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
                .size(24.dp)
                .clickable(onClick = openAppAction(addLink)),
        )
    }
}

@Composable
private fun TaskListBody(
    state: TaskListWidgetState,
    isDaily: Boolean,
    isLarge: Boolean,
    openListLink: String,
) {
    when {
        state.needsCron && isDaily -> StartDayCard(
            onClick = actionRunCallback<RunCronAction>(),
        )
        state.tasks.isEmpty() -> EmptyState(
            message = if (isDaily) "All done today!" else "All done!",
        )
        else -> TaskListRows(
            state = state,
            isDaily = isDaily,
            isLarge = isLarge,
            openListLink = openListLink,
        )
    }
}

@Composable
private fun TaskListRows(
    state: TaskListWidgetState,
    isDaily: Boolean,
    isLarge: Boolean,
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
                    TaskRow(
                        text = task.text,
                        valueColor = colorForTaskValue(task.value),
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
                    if (index < visible.size - 1 || (isLarge && remaining > 0)) {
                        TaskRowSeparator()
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
                    color = WidgetColors.dailiesPurple,
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
