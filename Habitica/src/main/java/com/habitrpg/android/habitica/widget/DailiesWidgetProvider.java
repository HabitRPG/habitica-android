package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.R;

public class DailiesWidgetProvider extends TaskListWidgetProvider{

    @Override
    protected Class getServiceClass() {
        return DailiesWidgetService.class;
    }

    @Override
    protected Class getProviderClass() {
        return DailiesWidgetProvider.class;
    }

    @Override
    protected int getTitleResId() {
        return R.string.dailies;
    }
}

