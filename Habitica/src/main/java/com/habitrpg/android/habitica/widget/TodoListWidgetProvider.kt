package com.habitrpg.android.habitica.widget

import androidx.glance.appwidget.GlanceAppWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget

class TodoListWidgetProvider : HydratingGlanceReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoTaskListGlanceWidget()
}
