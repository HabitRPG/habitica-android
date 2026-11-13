package com.habitrpg.android.habitica.widget

import androidx.glance.appwidget.GlanceAppWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget

class AvatarStatsWidgetProvider : HydratingGlanceReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AvatarStatsGlanceWidget()
}
