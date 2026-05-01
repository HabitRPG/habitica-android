package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.widget.AddTaskWidgetProvider
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTaskWidgetActivity : ComponentActivity() {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                AddTaskConfigContent(
                    onSelected = { type -> finishWithSelection(type) },
                )
            }
        }
    }

    private fun finishWithSelection(type: TaskType) {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putString("add_task_widget_$widgetId", type.value)
        }
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()

        val updateIntent = Intent(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            null,
            this,
            AddTaskWidgetProvider::class.java,
        ).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        sendBroadcast(updateIntent)
    }
}

@Composable
private fun AddTaskConfigContent(onSelected: (TaskType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Choose task type",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(24.dp))
        TaskTypeButton("Habit", Color(0xFFF23035)) { onSelected(TaskType.HABIT) }
        Spacer(Modifier.height(12.dp))
        TaskTypeButton("Daily", Color(0xFFFFA624)) { onSelected(TaskType.DAILY) }
        Spacer(Modifier.height(12.dp))
        TaskTypeButton("To Do", Color(0xFF26A0AB)) { onSelected(TaskType.TODO) }
        Spacer(Modifier.height(12.dp))
        TaskTypeButton("Reward", Color(0xFF1CA372)) { onSelected(TaskType.REWARD) }
    }
}

@Composable
private fun TaskTypeButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxSize().height(56.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
