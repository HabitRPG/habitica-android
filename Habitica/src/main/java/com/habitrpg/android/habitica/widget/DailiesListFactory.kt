package com.habitrpg.android.habitica.widget

import android.content.Context
import android.content.Intent

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.Task


class DailiesListFactory(context: Context, intent: Intent) : TaskListFactory(context, intent, Task.TYPE_DAILY, R.layout.widget_dailies_list_row, R.id.dailies_text)