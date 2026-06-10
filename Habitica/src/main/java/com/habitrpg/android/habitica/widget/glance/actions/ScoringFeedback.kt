package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import android.widget.Toast
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.widget.glance.work.WidgetSnapshotPublisher
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun showScoringToast(context: Context, result: TaskScoringResult?) {
    if (result == null) return
    val text = NotifyUserUseCase.getNotificationAndAddStatsToUserAsText(
        result.experienceDelta,
        result.healthDelta,
        result.goldDelta,
        result.manaDelta,
        result.questDamage,
    ).first
    withContext(Dispatchers.Main) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}

suspend fun applyInAppScoreToWidgets(context: Context, taskId: String, up: Boolean) {
    if (up) WidgetSnapshotPublisher.optimisticComplete(context, taskId)
    WidgetSnapshotPublisher.publishAll(context)
}
