package com.habitrpg.android.habitica.widget;

public class DailiesWidgetProvider extends TaskListWidgetProvider{

    @Override
    protected Class getServiceClass() {
        return DailiesWidgetService.class;
    }
}

