package com.habitrpg.android.habitica.widget;

import android.content.Context;
import android.content.Intent;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;


public class DailiesListFactory extends TaskListFactory {
    public DailiesListFactory(Context context, Intent intent) {
        super(context, intent, Task.TYPE_DAILY);
    }
}

