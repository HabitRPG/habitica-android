package com.habitrpg.android.habitica.widget

import android.content.Intent
import android.widget.RemoteViewsService

class TodosWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoListFactory(this.applicationContext, intent)
    }
}
