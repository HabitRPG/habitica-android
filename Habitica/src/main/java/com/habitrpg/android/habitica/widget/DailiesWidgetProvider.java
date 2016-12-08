package com.habitrpg.android.habitica.widget;

import android.content.Context;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;

import javax.inject.Inject;

public class DailiesWidgetProvider extends TaskListWidgetProvider{

    public static final String DAILY_ACTION = "com.habitrpg.android.habitica.DAILY_ACTION";
    public static final String TASK_ID_ITEM = "com.habitrpg.android.habitica.TASK_ID_ITEM";

    @Inject
    APIHelper apiHelper;
    @Inject
    HostConfig hostConfig;

    private void setUp(Context context) {
        if (apiHelper == null) {
            HabiticaBaseApplication application = HabiticaApplication.getInstance(context);
            application.getComponent().inject(this);
        }
    }

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

