package com.habitrpg.android.habitica.widget.glance.state

import androidx.glance.action.ActionParameters

object WidgetActionKeys {
    val taskId = ActionParameters.Key<String>("task_id")
    val direction = ActionParameters.Key<String>("direction")
    val deepLink = ActionParameters.Key<String>("deep_link")
}
