package com.habitrpg.android.habitica.widget

import com.habitrpg.android.habitica.R

class TodoListWidgetProvider : TaskListWidgetProvider() {
    override val serviceClass: Class<*>
        get() = TodosWidgetService::class.java

    override val providerClass: Class<*>
        get() = TodoListWidgetProvider::class.java

    override val titleResId: Int
        get() = R.string.todos
}
