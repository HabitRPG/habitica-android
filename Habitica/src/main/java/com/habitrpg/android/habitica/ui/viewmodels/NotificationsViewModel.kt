package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.common.habitica.models.notifications.GroupTaskRequiresApprovalData
import com.habitrpg.common.habitica.models.notifications.GuildInvitationData
import com.habitrpg.common.habitica.models.notifications.GuildInvite
import com.habitrpg.common.habitica.models.notifications.ItemReceivedData
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import com.habitrpg.common.habitica.models.notifications.NewStuffData
import com.habitrpg.common.habitica.models.notifications.PartyInvitationData
import com.habitrpg.common.habitica.models.notifications.PartyInvite
import com.habitrpg.common.habitica.models.notifications.QuestInvitationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class NotificationsViewModel @Inject constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val notificationsManager: NotificationsManager,
    val socialRepository: SocialRepository
) : BaseViewModel(userRepository, userViewModel) {

    private val supportedNotificationTypes = listOf(
        Notification.Type.NEW_STUFF.type,
        Notification.Type.NEW_CHAT_MESSAGE.type,
        Notification.Type.NEW_MYSTERY_ITEMS.type,
        Notification.Type.GROUP_TASK_NEEDS_WORK.type,
        Notification.Type.GROUP_TASK_APPROVED.type,
        Notification.Type.UNALLOCATED_STATS_POINTS.type,
        Notification.Type.ITEM_RECEIVED.type
    )

    private val actionableNotificationTypes = listOf(
        Notification.Type.GUILD_INVITATION.type,
        Notification.Type.PARTY_INVITATION.type,
        Notification.Type.QUEST_INVITATION.type
    )

    private var party: UserParty? = null

    private val customNotifications = MutableStateFlow<List<Notification>>(emptyList())

    init {
        userViewModel.user.observeForever {
            if (it == null) return@observeForever
            party = it.party
            val notifications = convertInvitationsToNotifications(it)
            if (it.flags?.newStuff == true) {
                val notification = Notification()
                notification.id = "custom-new-stuff-notification"
                notification.type = Notification.Type.NEW_STUFF.type
                val data = NewStuffData()
                notification.data = data
                notifications.add(notification)
            }
            customNotifications.value = notifications
        }
    }

    fun getNotifications(): Flow<List<Notification>> {
        val serverNotifications =
            notificationsManager.getNotifications().map { filterSupportedTypes(it) }

        return serverNotifications.combine(customNotifications) { serverNotificationsList, customNotificationsList ->
            if (serverNotificationsList.firstOrNull { notification -> notification.type == Notification.Type.NEW_STUFF.type } != null) {
                return@combine serverNotificationsList + customNotificationsList.filter { notification -> notification.type != Notification.Type.NEW_STUFF.type }
            }
            return@combine serverNotificationsList + customNotificationsList
        }.map { it.sortedBy { notification -> notification.priority } }
    }

    fun getNotificationCount(): Flow<Int> {
        return getNotifications().map { it.count() }.distinctUntilChanged()
    }

    fun allNotificationsSeen(): Flow<Boolean> {
        return getNotifications().map { it.all { notification -> notification.seen == true } }
            .distinctUntilChanged()
    }

    fun getHasPartyNotification(): Flow<Boolean> {
        return getNotifications().map {
            it.find { notification ->
                val data = notification.data as? NewChatMessageData
                isPartyMessage(data)
            } != null
        }.distinctUntilChanged()
    }

    suspend fun refreshNotifications(): User? {
        return userRepository.retrieveUser(withTasks = false, forced = true)
    }

    private fun filterSupportedTypes(notifications: List<Notification>): List<Notification> {
        return notifications.filter { supportedNotificationTypes.contains(it.type) }
    }

    private fun convertInvitationsToNotifications(user: User): MutableList<Notification> {
        val notifications = mutableListOf<Notification>()

        notifications.addAll(
            user.invitations?.parties?.map {
                val notification = Notification()
                notification.id = "custom-party-invitation-" + it.id
                notification.type = Notification.Type.PARTY_INVITATION.type
                val data = PartyInvitationData()
                data.invitation = PartyInvite()
                data.invitation?.id = it.id
                data.invitation?.name = it.name
                data.invitation?.inviter = it.inviter
                notification.data = data
                notification
            } ?: emptyList()
        )

        notifications.addAll(
            user.invitations?.guilds?.map {
                val notification = Notification()
                notification.id = "custom-guild-invitation-" + it.id
                notification.type = Notification.Type.GUILD_INVITATION.type
                val data = GuildInvitationData()
                data.invitation = GuildInvite()
                data.invitation?.id = it.id
                data.invitation?.name = it.name
                data.invitation?.inviter = it.inviter
                data.invitation?.publicGuild = it.publicGuild
                notification.data = data
                notification
            } ?: emptyList()
        )

        val quest = user.party?.quest
        if (quest != null && quest.RSVPNeeded) {
            val notification = Notification()
            notification.id = "custom-quest-invitation-" + user.party?.id
            notification.type = Notification.Type.QUEST_INVITATION.type
            val data = QuestInvitationData()
            data.questKey = quest.key
            notification.data = data

            notifications.add(notification)
        }

        return notifications
    }

    fun isPartyMessage(data: NewChatMessageData?): Boolean {
        if (party?.isValid != true || data?.group?.id == null) {
            return false
        }

        return party?.id == data.group?.id
    }

    /**
     * Is the given notification an "artificial" custom notification (created by this class)
     * instead of one of the ones coming from server.
     */
    private fun isCustomNotification(notification: Notification): Boolean {
        return notification.id.startsWith("custom-")
    }

    private fun isCustomNewStuffNotification(notification: Notification) =
        notification.id == "custom-new-stuff-notification"

    fun dismissNotification(notification: Notification) {
        if (isCustomNotification(notification)) {
            if (isCustomNewStuffNotification(notification)) {
                updateUser("flags.newStuff", false)
                customNotifications.value =
                    customNotifications.value.filterNot { it.id == notification.id }
            }
            return
        }

        viewModelScope.launchCatching {
            userRepository.readNotification(notification.id)
        }
    }

    fun dismissAllNotifications(notifications: List<Notification>) {
        val dismissableIds = notifications.filter { !isCustomNotification(it) }
            .filter { !actionableNotificationTypes.contains(it.type) }.map { it.id }

        val customNewStuffNotification =
            notifications.firstOrNull { isCustomNewStuffNotification(it) }

        if (customNewStuffNotification != null) {
            dismissNotification(customNewStuffNotification)
        }

        if (dismissableIds.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = dismissableIds

        viewModelScope.launchCatching {
            userRepository.readNotifications(notificationIds)
        }
    }

    fun markNotificationsAsSeen(notifications: List<Notification>) {
        val unseenIds =
            notifications.filter { !isCustomNotification(it) }.filter { it.seen == false }
                .map { it.id }

        if (unseenIds.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = unseenIds

        viewModelScope.launchCatching {
            userRepository.seeNotifications(notificationIds)
        }
    }

    private fun findNotification(id: String): Notification? {
        return notificationsManager.getNotification(id)
            ?: customNotifications.value.find { it.id == id }
    }

    fun click(notificationId: String, navController: MainNavigationController) {
        val notification = findNotification(notificationId) ?: return

        dismissNotification(notification)

        when (notification.type) {
            Notification.Type.NEW_STUFF.type -> navController.navigate(R.id.newsFragment)
            Notification.Type.NEW_CHAT_MESSAGE.type -> clickNewChatMessage(
                notification,
                navController
            )

            Notification.Type.GUILD_INVITATION.type -> clickGroupInvitation(
                notification,
                navController
            )

            Notification.Type.PARTY_INVITATION.type -> clickGroupInvitation(
                notification,
                navController
            )

            Notification.Type.QUEST_INVITATION.type -> navController.navigate(R.id.partyFragment)
            Notification.Type.NEW_MYSTERY_ITEMS.type -> navController.navigate(
                R.id.itemsFragment,
                bundleOf(Pair("itemType", "special"))
            )

            Notification.Type.UNALLOCATED_STATS_POINTS.type -> navController.navigate(R.id.statsFragment)
            // Group tasks should go to Group tasks view if that is added to this app at some point
            Notification.Type.GROUP_TASK_APPROVED.type -> navController.navigate(R.id.tasksFragment)
            Notification.Type.GROUP_TASK_NEEDS_WORK.type -> navController.navigate(R.id.tasksFragment)
            Notification.Type.ITEM_RECEIVED.type -> clickItemReceivedNotification(
                notification,
                navController
            )
        }
    }

    private fun clickItemReceivedNotification(
        notification: Notification,
        navController: MainNavigationController
    ) {
        val data = notification.data as? ItemReceivedData
        when (data?.destination) {
            "equipment" -> navController.navigate(R.id.equipmentOverviewFragment)
            "customization" -> navController.navigate(R.id.avatarCustomizationFragment)
            else -> navController.navigate(R.id.itemsFragment)
        }
    }

    private fun clickNewChatMessage(
        notification: Notification,
        navController: MainNavigationController
    ) {
        val data = notification.data as? NewChatMessageData
        if (isPartyMessage(data)) {
            val bundle = Bundle()
            bundle.putString("groupID", data?.group?.id)
            navController.navigate(R.id.partyFragment, bundle)
        } else {
            val bundle = Bundle()
            bundle.putString("groupID", data?.group?.id)
            bundle.putBoolean(
                "isMember",
                true
            ) // safe to assume user is member since they got the notification
            bundle.putInt("tabToOpen", 1)
            navController.navigate(R.id.guildFragment, bundle)
        }
    }

    private fun clickGroupInvitation(
        notification: Notification,
        navController: MainNavigationController
    ) {
        when (notification.type) {
            Notification.Type.GUILD_INVITATION.type -> {
                val bundle = Bundle()
                val data = notification.data as? GuildInvitationData
                bundle.putString("groupID", data?.invitation?.id)
                bundle.putBoolean(
                    "isMember",
                    true
                ) // safe to assume user is member since they got the notification
                navController.navigate(R.id.guildFragment, bundle)
            }

            Notification.Type.PARTY_INVITATION.type -> {
                navController.navigate(R.id.partyFragment)
            }
        }
    }

    fun accept(notificationId: String) {
        val notification = findNotification(notificationId) ?: return
        when (notification.type) {
            Notification.Type.GUILD_INVITATION.type -> {
                val data = notification.data as? GuildInvitationData
                acceptGroupInvitation(data?.invitation?.id)
            }

            Notification.Type.PARTY_INVITATION.type -> {
                val data = notification.data as? PartyInvitationData
                acceptGroupInvitation(data?.invitation?.id)
            }

            Notification.Type.QUEST_INVITATION.type -> acceptQuestInvitation()
            Notification.Type.GROUP_TASK_REQUIRES_APPROVAL.type -> acceptTaskApproval(notification)
        }
        if (isCustomNotification(notification)) {
            viewModelScope.launch(ExceptionHandler.coroutine()) {
                userRepository.retrieveUser()
            }
        } else {
            dismissNotification(notification)
        }
    }

    fun reject(notificationId: String) {
        val notification = findNotification(notificationId) ?: return
        when (notification.type) {
            Notification.Type.GUILD_INVITATION.type -> {
                val data = notification.data as? GuildInvitationData
                rejectGroupInvite(data?.invitation?.id)
            }

            Notification.Type.PARTY_INVITATION.type -> {
                val data = notification.data as? PartyInvitationData
                rejectGroupInvite(data?.invitation?.id)
            }

            Notification.Type.QUEST_INVITATION.type -> rejectQuestInvitation()
            Notification.Type.GROUP_TASK_REQUIRES_APPROVAL.type -> rejectTaskApproval(notification)
        }
        if (!isCustomNotification(notification)) {
            dismissNotification(notification)
        }
    }

    private fun acceptGroupInvitation(groupId: String?) {
        groupId?.let {
            viewModelScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.joinGroup(it)
                refreshUser()
            }
        }
    }

    fun rejectGroupInvite(groupId: String?) {
        groupId?.let {
            viewModelScope.launchCatching {
                socialRepository.rejectGroupInvite(it)
                refreshUser()
            }
        }
    }

    private fun acceptQuestInvitation() {
        party?.id?.let {
            viewModelScope.launchCatching {
                socialRepository.acceptQuest(null, it)
                refreshUser()
            }
        }
    }

    private fun rejectQuestInvitation() {
        party?.id?.let {
            viewModelScope.launchCatching {
                socialRepository.rejectQuest(null, it)
                refreshUser()
            }
        }
    }

    private fun refreshUser() {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            refreshNotifications()
        }
    }

    private fun acceptTaskApproval(notification: Notification) {
        notification.data as? GroupTaskRequiresApprovalData
    }

    private fun rejectTaskApproval(notification: Notification) {
        notification.data as? GroupTaskRequiresApprovalData
    }
}
