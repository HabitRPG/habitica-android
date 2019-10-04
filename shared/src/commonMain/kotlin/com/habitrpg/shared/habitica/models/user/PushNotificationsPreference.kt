package com.habitrpg.shared.habitica.models.user

expect open class PushNotificationsPreference {
    var unsubscribeFromAll: Boolean
    var invitedParty: Boolean
    var invitedQuest: Boolean
    var majorUpdates: Boolean
    var wonChallenge: Boolean
    var invitedGuild: Boolean
    var newPM: Boolean
    var questStarted: Boolean
    var giftedGems: Boolean
    var giftedSubscription: Boolean
    var partyActivity: Boolean
}
