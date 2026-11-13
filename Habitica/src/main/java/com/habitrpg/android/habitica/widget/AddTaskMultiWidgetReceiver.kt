package com.habitrpg.android.habitica.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskMultiGlanceWidget

class AddTaskMultiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AddTaskMultiGlanceWidget()
}
