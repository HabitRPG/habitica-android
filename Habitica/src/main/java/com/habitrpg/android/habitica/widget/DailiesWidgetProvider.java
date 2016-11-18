package com.habitrpg.android.habitica.widget;

public class DailiesWidgetProvider extends TaskListWidgetProvider{

    @Override
    protected Class getServiceClass() {
        return DailiesWidgetService.class;
    }

    @Override
    protected Class getProviderClass() {
        return DailiesWidgetProvider.class;
    }
}

