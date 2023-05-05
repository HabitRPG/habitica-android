package com.habitrpg.android.habitica.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TodosWidgetService : RemoteViewsService() {

    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var userRepository : UserRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoListFactory(this.applicationContext, intent, taskRepository, userRepository)
    }
}
