package com.habitrpg.android.habitica.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class DailiesWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DailiesListProvider(this.getApplicationContext(), intent);
    }
}
