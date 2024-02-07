package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class EmailNotificationsPreference : RealmObject(), BaseObject {
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
    var subscriptionReminders: Boolean = false
    var onboarding: Boolean = false
    var kickedGroup: Boolean = false
    var contentRelease: Boolean = false
}
