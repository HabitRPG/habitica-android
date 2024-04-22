package com.habitrpg.android.habitica.widget

import android.content.Context
import android.content.Intent
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.shared.habitica.models.tasks.TaskType

class TodoListFactory(
    context: Context,
    intent: Intent,
    taskRepository: TaskRepository,
    userRepository: UserRepository,
) : TaskListFactory(
        context,
        intent,
        TaskType.TODO,
        R.layout.widget_todo_list_row,
        R.id.todo_text,
        taskRepository,
        userRepository,
    )
