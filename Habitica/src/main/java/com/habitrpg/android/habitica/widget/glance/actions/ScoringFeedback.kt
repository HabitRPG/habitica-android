package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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

suspend fun applyAvatarStatOverrides(
    context: Context,
    user: User?,
    result: TaskScoringResult?,
) {
    if (user == null || result == null) return
    val stats = user.stats ?: return
    val realmHp = (stats.hp ?: 0.0).toFloat()
    val realmExp = (stats.exp ?: 0.0).toFloat()
    val realmMp = (stats.mp ?: 0.0).toFloat()
    val realmGold = stats.gp ?: 0.0

    val manager = GlanceAppWidgetManager(context)
    val ids = manager.getGlanceIds(AvatarStatsGlanceWidget::class.java)
    for (id in ids) {
        updateAppWidgetState(context, id) { prefs ->
            val baseHp = prefs[WidgetStateKeys.statOverrideHp] ?: realmHp
            val baseExp = prefs[WidgetStateKeys.statOverrideExp] ?: realmExp
            val baseMp = prefs[WidgetStateKeys.statOverrideMp] ?: realmMp
            val baseGold = prefs[WidgetStateKeys.statOverrideGold] ?: realmGold
            prefs[WidgetStateKeys.statOverrideHp] = baseHp + result.healthDelta.toFloat()
            prefs[WidgetStateKeys.statOverrideExp] = baseExp + result.experienceDelta.toFloat()
            prefs[WidgetStateKeys.statOverrideMp] = baseMp + result.manaDelta.toFloat()
            prefs[WidgetStateKeys.statOverrideGold] = baseGold + result.goldDelta
            prefs[WidgetStateKeys.statOverrideValid] = true
        }
    }
}

suspend fun refreshStatsDependentWidgets(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val widgets: List<GlanceAppWidget> = listOf(
        AvatarStatsGlanceWidget(),
        DailiesCountGlanceWidget(),
        DailyTaskListGlanceWidget(),
        TodoTaskListGlanceWidget(),
    )
    widgets.forEach { widget ->
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            widget.update(context, id)
        }
    }
}

suspend fun applyInAppScoreToWidgets(context: Context, taskId: String) {
    val manager = GlanceAppWidgetManager(context)
    val listTargets = buildList {
        addAll(manager.getGlanceIds(DailyTaskListGlanceWidget::class.java))
        addAll(manager.getGlanceIds(TodoTaskListGlanceWidget::class.java))
        addAll(manager.getGlanceIds(DailiesCountGlanceWidget::class.java))
    }
    for (id in listTargets) {
        updateAppWidgetState(context, id) { prefs ->
            val existing = prefs[WidgetStateKeys.taskListHiddenIds] ?: emptySet()
            prefs[WidgetStateKeys.taskListHiddenIds] = existing + taskId
        }
    }

    val stats = withContext(Dispatchers.Main) {
        widgetEntryPoint(context).userRepository().getUser().firstOrNull()?.stats
    }
    if (stats != null) {
        manager.getGlanceIds(AvatarStatsGlanceWidget::class.java).forEach { id ->
            updateAppWidgetState(context, id) { prefs ->
                prefs[WidgetStateKeys.statOverrideHp] = (stats.hp ?: 0.0).toFloat()
                prefs[WidgetStateKeys.statOverrideExp] = (stats.exp ?: 0.0).toFloat()
                prefs[WidgetStateKeys.statOverrideMp] = (stats.mp ?: 0.0).toFloat()
                prefs[WidgetStateKeys.statOverrideGold] = stats.gp ?: 0.0
                prefs[WidgetStateKeys.statOverrideValid] = true
            }
        }
    }
}
