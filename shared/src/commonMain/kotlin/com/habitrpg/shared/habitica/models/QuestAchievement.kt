package com.habitrpg.shared.habitica.models


expect open class QuestAchievement {
    var combinedKey: String?

    var questKey: String?
    var userID: String?
    var count: Int

    var title: String?
}
