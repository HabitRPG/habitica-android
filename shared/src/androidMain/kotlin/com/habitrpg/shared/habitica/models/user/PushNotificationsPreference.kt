package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject

actual open class PushNotificationsPreference : RealmObject() {
    actual var unsubscribeFromAll: Boolean = false
    actual var invitedParty: Boolean = false
    actual var invitedQuest: Boolean = false
    actual var majorUpdates: Boolean = false
    actual var wonChallenge: Boolean = false
    actual var invitedGuild: Boolean = false
    actual var newPM: Boolean = false
    actual var questStarted: Boolean = false
    actual var giftedGems: Boolean = false
    actual var giftedSubscription: Boolean = false
    actual var partyActivity: Boolean = false
}
