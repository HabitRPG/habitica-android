package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import android.content.Context;
import android.content.Intent;

public class TodoListFactory extends TaskListFactory {
    public TodoListFactory(Context context, Intent intent) {
        super(context, intent, Task.TYPE_TODO, R.layout.widget_todo_list_row, R.id.todo_text);
    }
}
