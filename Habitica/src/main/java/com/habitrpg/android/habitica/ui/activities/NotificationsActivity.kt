package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityNotificationsBinding
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.common.habitica.models.notifications.GroupTaskApprovedData
import com.habitrpg.common.habitica.models.notifications.GroupTaskNeedsWorkData
import com.habitrpg.common.habitica.models.notifications.GroupTaskRequiresApprovalData
import com.habitrpg.common.habitica.models.notifications.GuildInvitationData
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import com.habitrpg.common.habitica.models.notifications.NewStuffData
import com.habitrpg.common.habitica.models.notifications.PartyInvitationData
import com.habitrpg.common.habitica.models.notifications.QuestInvitationData
import com.habitrpg.common.habitica.models.notifications.UnallocatedPointsData
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import javax.inject.Inject

class NotificationsActivity : BaseActivity(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityNotificationsBinding
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var socialRepository: SocialRepository

    val viewModel: NotificationsViewModel by viewModels()

    var inflater: LayoutInflater? = null

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    override fun getContentView(): View {
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        return binding.root
    }

    private var notifications: List<Notification> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar)

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater

        compositeSubscription.add(
            viewModel.getNotifications().subscribe(
                {
                    this.setNotifications(it)
                    viewModel.markNotificationsAsSeen(it)
                },
                RxErrorHandler.handleEmptyError()
            )
        )

        binding.notificationsRefreshLayout.setOnRefreshListener(this)
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onRefresh() {
        binding.notificationsRefreshLayout.isRefreshing = true

        compositeSubscription.add(
            viewModel.refreshNotifications().subscribe(
                {
                    binding.notificationsRefreshLayout.isRefreshing = false
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    private fun setNotifications(notifications: List<Notification>) {
        this.notifications = notifications

        binding.notificationItems.removeAllViewsInLayout()

        if (notifications.isEmpty()) {
            displayNoNotificationsView()
        } else {
            displayNotificationsListView(notifications)
        }
    }

    private fun displayNoNotificationsView() {
        binding.notificationItems.showDividers = LinearLayout.SHOW_DIVIDER_NONE

        binding.notificationItems.addView(inflater?.inflate(R.layout.no_notifications, binding.notificationItems, false))
    }

    private fun displayNotificationsListView(notifications: List<Notification>) {
        binding.notificationItems.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE or LinearLayout.SHOW_DIVIDER_END

        binding.notificationItems.addView(
            createNotificationsHeaderView(notifications.count())
        )

        notifications.map {
            val item: View? = when (it.type) {
                Notification.Type.NEW_CHAT_MESSAGE.type -> createNewChatMessageNotification(it)
                Notification.Type.NEW_STUFF.type -> createNewStuffNotification(it)
                Notification.Type.UNALLOCATED_STATS_POINTS.type -> createUnallocatedStatsNotification(it)
                Notification.Type.NEW_MYSTERY_ITEMS.type -> createMysteryItemsNotification(it)
                Notification.Type.GROUP_TASK_NEEDS_WORK.type -> createGroupTaskNeedsWorkNotification(it)
                Notification.Type.GROUP_TASK_APPROVED.type -> createGroupTaskApprovedNotification(it)
                Notification.Type.GROUP_TASK_REQUIRES_APPROVAL.type -> createGroupTaskNeedsApprovalNotification(it)
                Notification.Type.PARTY_INVITATION.type -> createPartyInvitationNotification(it)
                Notification.Type.GUILD_INVITATION.type -> createGuildInvitationNotification(it)
                Notification.Type.QUEST_INVITATION.type -> createQuestInvitationNotification(it)
                else -> null
            }

            if (item != null) {
                binding.notificationItems.addView(item)
            }
        }
    }

    private fun createNotificationsHeaderView(notificationCount: Int): View? {
        val header = inflater?.inflate(R.layout.notifications_header, binding.notificationItems, false)

        val badge = header?.findViewById(R.id.notifications_title_badge) as? TextView
        badge?.text = notificationCount.toString()

        val dismissAllButton = header?.findViewById(R.id.dismiss_all_button) as? Button
        dismissAllButton?.setOnClickListener { viewModel.dismissAllNotifications(notifications) }

        return header
    }

    private fun createNewChatMessageNotification(notification: Notification): View? {
        val data = notification.data as? NewChatMessageData
        val stringId = if (viewModel.isPartyMessage(data)) R.string.new_msg_party else R.string.new_msg_guild

        return createDismissableNotificationItem(
            notification,
            fromHtml(getString(stringId, data?.group?.name))
        )
    }

    private fun createNewStuffNotification(notification: Notification): View? {
        val data = notification.data as? NewStuffData
        val text = if (data?.title != null) {
            fromHtml("<b>" + getString(R.string.new_bailey_update) + "</b><br>" + data.title)
        } else {
            fromHtml("<b>" + getString(R.string.new_bailey_update) + "</b>")
        }

        return createDismissableNotificationItem(
            notification,
            text,
            R.drawable.notifications_bailey
        )
    }

    private fun createUnallocatedStatsNotification(notification: Notification): View? {
        val data = notification.data as? UnallocatedPointsData

        return createDismissableNotificationItem(
            notification,
            fromHtml(getString(R.string.unallocated_stats_points, data?.points.toString())),
            R.drawable.notification_stat_sparkles
        )
    }

    private fun createMysteryItemsNotification(notification: Notification): View? {
        return createDismissableNotificationItem(
            notification,
            fromHtml(getString(R.string.new_subscriber_item)),
            R.drawable.notification_mystery_item
        )
    }

    private fun createGroupTaskNeedsWorkNotification(notification: Notification): View? {
        val data = notification.data as? GroupTaskNeedsWorkData
        val message = convertGroupMessageHtml(data?.message ?: "")

        return createDismissableNotificationItem(
            notification,
            fromHtml(message),
            null,
            R.color.yellow_5
        )
    }

    private fun createGroupTaskApprovedNotification(notification: Notification): View? {
        val data = notification.data as? GroupTaskApprovedData
        val message = convertGroupMessageHtml(data?.message ?: "")

        return createDismissableNotificationItem(
            notification,
            fromHtml(message),
            null,
            R.color.green_10
        )
    }

    private fun createGroupTaskNeedsApprovalNotification(notification: Notification): View? {
        val data = notification.data as? GroupTaskRequiresApprovalData
        val message = convertGroupMessageHtml(data?.message ?: "")

        val item = createActionableNotificationItem(
            notification,
            fromHtml(message)
        )
        // Hide for now
        item?.visibility = View.GONE
        return item
    }

    /**
     * Group task notifications have the message text in the notification data as HTML
     * with <span class="notification-bold"> tags around emphasized words. So we just
     * convert the span-tags to strong-tags to display correct parts as bold, since
     * Html.fromHtml does not support CSS.
     */
    private fun convertGroupMessageHtml(message: String): String {
        // Using positive lookbehind to make sure "span" is preceded by "<" or "</"
        val pattern = "(?<=</?)span".toRegex()

        return message.replace(pattern, "strong")
    }

    private fun createDismissableNotificationItem(
        notification: Notification,
        messageText: CharSequence,
        imageResourceId: Int? = null,
        textColor: Int? = null
    ): View? {
        val item = inflater?.inflate(R.layout.notification_item, binding.notificationItems, false)

        val container = item?.findViewById(R.id.notification_item) as? View
        container?.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("notificationId", notification.id)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        val dismissButton = item?.findViewById(R.id.dismiss_button) as? ImageView
        dismissButton?.setOnClickListener { viewModel.dismissNotification(notification) }

        val messageTextView = item?.findViewById(R.id.message_text) as? TextView
        messageTextView?.text = messageText

        if (imageResourceId != null) {
            val notificationImage = item?.findViewById(R.id.notification_image) as? ImageView
            notificationImage?.setImageResource(imageResourceId)
            notificationImage?.visibility = View.VISIBLE
        }

        if (textColor != null) {
            messageTextView?.setTextColor(ContextCompat.getColor(this, textColor))
        }

        return item
    }

    private fun createPartyInvitationNotification(notification: Notification): View? {
        val data = notification.data as? PartyInvitationData

        return createActionableNotificationItem(
            notification,
            fromHtml(getString(R.string.invited_to_party_notification, data?.invitation?.name))
        )
    }

    private fun createGuildInvitationNotification(notification: Notification): View? {
        val data = notification.data as? GuildInvitationData
        val stringId = if (data?.invitation?.publicGuild == false) R.string.invited_to_private_guild else R.string.invited_to_public_guild

        return createActionableNotificationItem(
            notification,
            fromHtml(getString(stringId, data?.invitation?.name)),
            data?.invitation?.publicGuild == true
        )
    }

    private fun createQuestInvitationNotification(notification: Notification): View? {
        val data = notification.data as? QuestInvitationData

        val view = createActionableNotificationItem(notification, "", true)

        // hide view until we have loaded quest data and populated the values
        view?.visibility = View.GONE

        compositeSubscription.add(
            inventoryRepository.getQuestContent(data?.questKey ?: "")
                .firstElement()
                .subscribe(
                    {
                        updateQuestInvitationView(view, it)
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )

        return view
    }

    private fun updateQuestInvitationView(view: View?, questContent: QuestContent) {
        val messageTextView = view?.findViewById(R.id.message_text) as? TextView
        messageTextView?.text = fromHtml(getString(R.string.invited_to_quest, questContent.text))

        val questObjectiveLabelView = view?.findViewById(R.id.quest_objective_label) as? TextView
        val questObjectiveTextView = view?.findViewById(R.id.quest_objective_text) as? TextView
        val questDifficultyLabelView = view?.findViewById(R.id.difficulty_label) as? TextView
        questDifficultyLabelView?.text = getText(R.string.difficulty)
        questDifficultyLabelView?.append(":")
        val questDifficultyView = view?.findViewById(R.id.quest_difficulty) as? RatingBar

        if (questContent.isBossQuest) {
            questObjectiveLabelView?.text = getString(R.string.defeat)
            questObjectiveTextView?.text = questContent.boss?.name

            questDifficultyView?.rating = questContent.boss?.str ?: 1f
        } else {
            questObjectiveLabelView?.text = getString(R.string.collect)
            val collectionList = questContent.collect?.map { it.count.toString() + " " + it.text }
            questObjectiveTextView?.text = collectionList?.joinToString(", ")

            questDifficultyView?.rating = 1f
        }

        questObjectiveLabelView?.append(":")
        val questDetailView = view?.findViewById(R.id.quest_detail_view) as? View
        questDetailView?.visibility = View.VISIBLE
        view?.visibility = View.VISIBLE
    }

    private fun createActionableNotificationItem(
        notification: Notification,
        messageText: CharSequence,
        openable: Boolean = false
    ): View? {
        val item = inflater?.inflate(R.layout.notification_item_actionable, binding.notificationItems, false)

        if (openable) {
            val container = item?.findViewById(R.id.notification_item) as? View
            container?.setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putExtra("notificationId", notification.id)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        val acceptButton = item?.findViewById(R.id.accept_button) as? Button
        acceptButton?.setOnClickListener {
            viewModel.accept(notification.id)
        }

        val rejectButton = item?.findViewById(R.id.reject_button) as? Button
        rejectButton?.setOnClickListener {
            viewModel.reject(notification.id)
        }

        val messageTextView = item?.findViewById(R.id.message_text) as? TextView
        messageTextView?.text = messageText

        return item
    }

    private fun fromHtml(text: String): CharSequence {
        return text.fromHtml()
    }
}
