package com.habitrpg.android.habitica.widget

import com.habitrpg.android.habitica.R

class DailiesWidgetProvider : TaskListWidgetProvider() {
    override val serviceClass: Class<*>
        get() = DailiesWidgetService::class.java
    override val providerClass: Class<*>
        get() = DailiesWidgetProvider::class.java
    override val titleResId: Int
        get() = R.string.dailies
}
