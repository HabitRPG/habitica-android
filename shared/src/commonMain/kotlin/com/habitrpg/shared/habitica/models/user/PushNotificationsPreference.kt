package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject

open class PushNotificationsPreference : NativeRealmObject() {
    var unsubscribeFromAll: Boolean = false
    var invitedParty: Boolean = false
    var invitedQuest: Boolean = false
    var majorUpdates: Boolean = false
    var wonChallenge: Boolean = false
    var invitedGuild: Boolean = false
    var newPM: Boolean = false
    var questStarted: Boolean = false
    var giftedGems: Boolean = false
    var giftedSubscription: Boolean = false
    var partyActivity: Boolean = false
    var mentionParty: Boolean = false
    var mentionJoinedGuild: Boolean = false
    var mentionUnjoinedGuild: Boolean = false
}
