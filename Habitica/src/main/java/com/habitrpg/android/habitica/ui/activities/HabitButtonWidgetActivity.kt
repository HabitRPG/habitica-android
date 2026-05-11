package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.widget.glance.data.HabitButtonWidgetCache
import com.habitrpg.android.habitica.widget.glance.theme.colorForHabitValueLight
import com.habitrpg.android.habitica.widget.glance.theme.colorForHabitValueMedium
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import com.habitrpg.android.habitica.widget.glance.work.HabitButtonRefreshWorker
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HabitButtonWidgetActivity : ComponentActivity() {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        Log.d("HabitButtonWidget", "ConfigActivity onCreate widgetId=$widgetId action=${intent?.action}")
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(
            Activity.RESULT_CANCELED,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        )

        setContent {
            val context = LocalContext.current
            val isDark = isSystemInDarkTheme()
            val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) darkColorScheme() else lightColorScheme()
            }
            MaterialTheme(colorScheme = colors) {
                var habits by remember { mutableStateOf<List<Task>>(emptyList()) }
                LaunchedEffect(Unit) {
                    habits = taskRepository.getTasks(
                        TaskType.HABIT,
                        includedGroupIDs = emptyArray(),
                    ).firstOrNull().orEmpty()
                }
                HabitPickerSheet(
                    habits = habits,
                    onSelected = { task -> finishWithSelection(task) },
                    onDismissed = { finish() },
                )
            }
        }
    }

    private fun finishWithSelection(task: Task) {
        if (task.id == null) return
        val appContext = applicationContext
        Log.d("HabitButtonWidget", "finishWithSelection widgetId=$widgetId taskId=${task.id}")

        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        )

        val capturedWidgetId = widgetId
        lifecycleScope.launch {
            runCatching {
                val glanceId = GlanceAppWidgetManager(appContext).getGlanceIdBy(capturedWidgetId)
                HabitButtonWidgetCache.write(appContext, glanceId, task)
                HabitButtonGlanceWidget().update(appContext, glanceId)
            }
            finish()
        }
        HabitButtonRefreshWorker.enqueue(appContext)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitPickerSheet(
    habits: List<Task>,
    onSelected: (Task) -> Unit,
    onDismissed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissed,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        SheetContent(habits = habits, onSelected = onSelected)
    }
}

@Composable
private fun SheetContent(
    habits: List<Task>,
    onSelected: (Task) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Choose a habit",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "The widget will show this habit's name and let you score it from your home screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        if (habits.isEmpty()) {
            EmptyHabitsState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = habits, key = { it.id ?: it.text }) { habit ->
                    HabitRow(habit = habit, onClick = { onSelected(habit) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyHabitsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "You don't have any habits yet. Create one in the app first.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HabitRow(habit: Task, onClick: () -> Unit) {
    val barColor = colorForHabitValueLight(habit.value)
    val circleColor = colorForHabitValueMedium(habit.value)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DirectionsPreview(
            showUp = habit.up == true,
            showDown = habit.down == true,
            barColor = barColor,
            circleColor = circleColor,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = habit.text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DirectionsPreview(
    showUp: Boolean,
    showDown: Boolean,
    barColor: Color,
    circleColor: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(barColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDown) DirectionGlyph(label = "−", circleColor = circleColor)
        if (showUp) DirectionGlyph(label = "+", circleColor = circleColor)
    }
}

@Composable
private fun DirectionGlyph(label: String, circleColor: Color) {
    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(circleColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
