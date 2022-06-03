package com.habitrpg.common.habitica.models.notifications

open class LoginIncentiveData : NotificationData {

    var message: String? = null
    var nextRewardAt: Int? = null
    var rewardText: String? = null
    var rewardKey: List<String>? = null
    var reward: List<Reward>? = null
}
