package com.habitrpg.android.habitica.widget;

public class TodoListWidgetProvider extends TaskListWidgetProvider{

    @Override
    protected Class getServiceClass() {
        return TodosWidgetService.class;
    }

    @Override
    protected Class getProviderClass() {
        return TodoListWidgetProvider.class;
    }
}
