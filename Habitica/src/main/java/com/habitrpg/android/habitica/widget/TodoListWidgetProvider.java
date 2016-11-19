package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.R;

public class TodoListWidgetProvider extends TaskListWidgetProvider{

    @Override
    protected Class getServiceClass() {
        return TodosWidgetService.class;
    }

    @Override
    protected Class getProviderClass() {
        return TodoListWidgetProvider.class;
    }

    @Override
    protected int getTitleResId() {
        return R.string.todos;
    }
}
