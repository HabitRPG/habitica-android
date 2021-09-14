package com.habitrpg.android.habitica.widget

import android.content.Intent
import android.widget.RemoteViewsService

class DailiesWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DailiesListFactory(this.applicationContext, intent)
    }
}
