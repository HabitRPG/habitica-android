package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class LoginIncentiveData : NotificationData {
    var message: String? = null
    var nextRewardAt: Int? = null
    var rewardText: String? = null
    var rewardKey: List<String>? = null
    var reward: List<Reward>? = null
}
