package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskSingleGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskSingleGlanceWidget.Companion.TASK_TYPE_KEY
import com.habitrpg.android.habitica.widget.glance.work.AddTaskRefreshWorker
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddTaskWidgetActivity : ComponentActivity() {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        Log.d("AddTaskWidget", "ConfigActivity onCreate widgetId=$widgetId action=${intent?.action}")
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
                AddTaskConfigSheet(
                    onSelected = { type -> finishWithSelection(type) },
                    onDismissed = { finish() },
                )
            }
        }
    }

    private fun finishWithSelection(type: TaskType) {
        val appContext = applicationContext
        Log.d("AddTaskWidget", "finishWithSelection widgetId=$widgetId type=${type.value}")

        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        )

        val capturedWidgetId = widgetId
        lifecycleScope.launch {
            runCatching {
                val glanceId = GlanceAppWidgetManager(appContext).getGlanceIdBy(capturedWidgetId)
                updateAppWidgetState(appContext, glanceId) { prefs ->
                    prefs[stringPreferencesKey(TASK_TYPE_KEY)] = type.value
                }
                AddTaskSingleGlanceWidget().update(appContext, glanceId)
            }
            finish()
        }
        AddTaskRefreshWorker.enqueue(appContext)
    }
}

private data class TaskTypeChoice(
    val type: TaskType,
    val labelResId: Int,
    val iconResId: Int,
    val brandColor: Color,
)

private val TASK_TYPE_CHOICES = listOf(
    TaskTypeChoice(TaskType.HABIT, R.string.habit, R.drawable.widget_add_habit_glyph, Color(0xFFF23035)),
    TaskTypeChoice(TaskType.DAILY, R.string.daily, R.drawable.widget_add_daily_glyph, Color(0xFFFFA624)),
    TaskTypeChoice(TaskType.TODO, R.string.todo, R.drawable.widget_add_todo_glyph, Color(0xFF26A0AB)),
    TaskTypeChoice(TaskType.REWARD, R.string.reward, R.drawable.widget_add_reward_glyph, Color(0xFF1CA372)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskConfigSheet(
    onSelected: (TaskType) -> Unit,
    onDismissed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissed,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        SheetContent(onSelected = onSelected)
    }
}

@Composable
private fun SheetContent(onSelected: (TaskType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.widget_config_add_task_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.widget_config_add_task_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(TASK_TYPE_CHOICES) { choice ->
                TaskTypeTile(choice = choice, onClick = { onSelected(choice.type) })
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TaskTypeTile(choice: TaskTypeChoice, onClick: () -> Unit) {
    val useDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val tileColor = if (useDynamic) MaterialTheme.colorScheme.primaryContainer else choice.brandColor
    val iconTint = if (useDynamic) MaterialTheme.colorScheme.onPrimaryContainer else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.widget_tile_scallop),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(tileColor),
            )
            Image(
                painter = painterResource(choice.iconResId),
                contentDescription = stringResource(choice.labelResId),
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(iconTint),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(choice.labelResId),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
