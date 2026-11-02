package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class AchievementData : NotificationData {
    val isLastOnboardingAchievement: Boolean = false
    var achievement: String? = null
    var icon: String? = null
    var message: String? = null
    var modalText: String? = null
    var count: Int? = null

    val iconName: String?
        get() = icon ?: achievement
}
