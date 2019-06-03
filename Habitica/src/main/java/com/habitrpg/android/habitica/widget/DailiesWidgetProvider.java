package com.habitrpg.android.habitica.widget;

import android.content.Context;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.api.HostConfig;
import com.habitrpg.android.habitica.data.ApiClient;

import java.util.Objects;

import javax.inject.Inject;

public class DailiesWidgetProvider extends TaskListWidgetProvider {

    public static final String DAILY_ACTION = "com.habitrpg.android.habitica.DAILY_ACTION";
    public static final String TASK_ID_ITEM = "com.habitrpg.android.habitica.TASK_ID_ITEM";

    @Inject
    ApiClient apiClient;
    @Inject
    HostConfig hostConfig;

    private void setUp(Context context) {
        if (apiClient == null) {
            Objects.requireNonNull(HabiticaBaseApplication.Companion.getUserComponent()).inject(this);
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

