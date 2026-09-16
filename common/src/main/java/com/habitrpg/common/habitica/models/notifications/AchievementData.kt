package com.habitrpg.common.habitica.models.notifications

open class AchievementData : NotificationData {
    val isLastOnboardingAchievement: Boolean = false
    var achievement: String? = null
    var icon: String? = null
    var message: String? = null
    var modalText: String? = null

    val iconName: String?
        get() = icon ?: achievement
}
