package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

object WidgetDataCoordinator {
    private const val DEBOUNCE_MS = 250L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun start(context: Context) {
        if (job?.isActive == true) return
        val appContext = context.applicationContext
        job = scope.launch(ExceptionHandler.coroutine()) {
            val entry = widgetEntryPoint(appContext)
            entry.userRepository().getUser()
                .flatMapLatest { user ->
                    if (user == null) {
                        flowOf(Unit)
                    } else {
                        val repo = entry.taskRepository()
                        val flows = listOf(
                            repo.getTasks(TaskType.DAILY, user.id, emptyArray()),
                            repo.getTasks(TaskType.TODO, user.id, emptyArray()),
                            repo.getTasks(TaskType.HABIT, user.id, emptyArray()),
                        )
                        combine(flows) { Unit }
                    }
                }
                .debounce(DEBOUNCE_MS)
                .collectLatest {
                    if (WidgetAuth.isLoggedIn(appContext)) {
                        runCatching { WidgetSnapshotPublisher.publishAll(appContext) }
                    }
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
