package com.habitrpg.android.habitica.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class TodosWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodoListProvider(this.getApplicationContext(), intent);
    }
}
