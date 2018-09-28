package com.habitrpg.android.habitica.helpers

import android.content.Intent
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.PrefsActivity
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.PreferencesFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.PreferencesFragment_MembersInjector
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment
import com.habitrpg.android.habitica.ui.fragments.social.InboxFragment
import com.habitrpg.android.habitica.ui.fragments.social.QuestDetailFragment

class NotificationOpenHandler {

    companion object {

        fun handleOpenedByNotification(identifier: String, intent: Intent, activity: MainActivity, user: User?) {
            when (identifier) {
                PushNotificationManager.PARTY_INVITE_PUSH_NOTIFICATION_KEY -> openPartyScreen(activity)
                PushNotificationManager.QUEST_BEGUN_PUSH_NOTIFICATION_KEY -> openQuestDetailSCreen(activity,
                        user?.party?.id,
                        user?.party?.quest?.key)
                PushNotificationManager.QUEST_INVITE_PUSH_NOTIFICATION_KEY -> openQuestDetailSCreen(activity,
                        user?.party?.id,
                        user?.party?.quest?.key)
                PushNotificationManager.GUILD_INVITE_PUSH_NOTIFICATION_KEY -> openGuildDetailScreen(activity,
                        intent.getStringExtra("groupID"))
                PushNotificationManager.RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY -> openPrivateMessageScreen(activity,
                        intent.getStringExtra("replyTo"))
                PushNotificationManager.CHANGE_USERNAME_PUSH_NOTIFICATION_KEY -> openSettingsScreen(activity)
            }
        }

        private fun openPrivateMessageScreen(activity: MainActivity, userID: String?) {
            if (userID?.isNotEmpty() == true) {
                return
            }
            val fragment = InboxFragment()
            fragment.userId = userID ?: ""
            activity.displayFragment(fragment)
        }

        private fun openPartyScreen(activity: MainActivity) {
            activity.selectMenuItem(NavigationDrawerFragment.SIDEBAR_PARTY)
        }

        private fun openQuestDetailSCreen(activity: MainActivity, partyId: String?, questKey: String?) {
            if (partyId?.isNotEmpty() == true || questKey?.isNotEmpty() == true) {
                return
            }
            val fragment = QuestDetailFragment()
            fragment.partyId = partyId
            fragment.questKey = questKey
            activity.displayFragment(fragment)
        }

        private fun openGuildDetailScreen(activity: MainActivity, groupID: String) {
            if (groupID.isEmpty()) {
                return
            }
            val fragment = GuildFragment()
            fragment.setGuildId(groupID)
            activity.displayFragment(fragment)
        }

        private fun openSettingsScreen(activity: MainActivity) {
            val passUserId = Intent(activity, PrefsActivity::class.java)
            passUserId.putExtra("userId", activity.userID)
            activity.startActivity(passUserId)
        }
    }
}