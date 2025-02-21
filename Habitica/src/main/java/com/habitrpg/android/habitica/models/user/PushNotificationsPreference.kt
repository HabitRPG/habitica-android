package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class PushNotificationsPreference : RealmObject(), BaseObject {
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
    var contentRelease: Boolean = false

    fun listOfEnabledKeys(): List<String> {
        var enabled = mutableListOf<String>()
        if (invitedParty) enabled.add("invitedParty")
        if (invitedQuest) enabled.add("invitedQuest")
        if (majorUpdates) enabled.add("majorUpdates")
        if (wonChallenge) enabled.add("wonChallenge")
        if (invitedGuild) enabled.add("invitedGuild")
        if (newPM) enabled.add("newPM")
        if (questStarted) enabled.add("questStarted")
        if (giftedGems) enabled.add("giftedGems")
        if (partyActivity) enabled.add("partyActivity")
        if (mentionParty) enabled.add("mentionParty")
        if (mentionJoinedGuild) enabled.add("mentionJoinedGuild")
        if (contentRelease) enabled.add("contentRelease")
        return enabled
    }

    fun mapOfKeys(): Map<String, Boolean> {
        return mapOf(
            "unsubscribeFromAll" to unsubscribeFromAll,
            "invitedParty" to invitedParty,
            "invitedQuest" to invitedQuest,
            "majorUpdates" to majorUpdates,
            "wonChallenge" to wonChallenge,
            "invitedGuild" to invitedGuild,
            "newPM" to newPM,
            "questStarted" to questStarted,
            "giftedGems" to giftedGems,
            "giftedSubscription" to giftedSubscription,
            "partyActivity" to partyActivity,
            "mentionParty" to mentionParty,
            "mentionJoinedGuild" to mentionJoinedGuild,
            "mentionUnjoinedGuild" to mentionUnjoinedGuild,
            "contentRelease" to contentRelease
        )
    }
}
