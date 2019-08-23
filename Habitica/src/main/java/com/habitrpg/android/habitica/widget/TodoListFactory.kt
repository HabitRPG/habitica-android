package com.habitrpg.android.habitica.widget

import android.content.Context
import android.content.Intent

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.Task

class TodoListFactory(context: Context, intent: Intent) : TaskListFactory(context, intent, Task.TYPE_TODO, R.layout.widget_todo_list_row, R.id.todo_text)
