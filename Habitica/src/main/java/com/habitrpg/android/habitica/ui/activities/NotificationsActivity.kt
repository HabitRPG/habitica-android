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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityNotificationsBinding
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import com.habitrpg.common.habitica.extensions.fadeInAnimation
import com.habitrpg.common.habitica.extensions.flash
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.extensions.observeOnce
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.common.habitica.models.notifications.GroupTaskApprovedData
import com.habitrpg.common.habitica.models.notifications.GroupTaskNeedsWorkData
import com.habitrpg.common.habitica.models.notifications.GroupTaskRequiresApprovalData
import com.habitrpg.common.habitica.models.notifications.GuildInvitationData
import com.habitrpg.common.habitica.models.notifications.ItemReceivedData
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import com.habitrpg.common.habitica.models.notifications.NewStuffData
import com.habitrpg.common.habitica.models.notifications.PartyInvitationData
import com.habitrpg.common.habitica.models.notifications.QuestInvitationData
import com.habitrpg.common.habitica.models.notifications.UnallocatedPointsData
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView
import com.habitrpg.common.habitica.views.PixelArtView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsActivity :
    BaseActivity(),
    androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityNotificationsBinding

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var socialRepository: SocialRepository

    val viewModel: NotificationsViewModel by viewModels()

    var inflater: LayoutInflater? = null
    var userLvl: Int? = null

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        return binding.root
    }

    private var notifications: List<Notification> = emptyList()

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar)

        // Check user level to handle if a user loses hp and drops below necessary level to allocate points -
        // and if so, don't display the notification to allocate points.
        viewModel.user.observeOnce(this) { user ->
            userLvl = user?.stats?.lvl ?: 0
        }

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater

        binding.progressView.setContent {
            HabiticaCircularProgressView(indicatorSize = 60.dp)
        }

        lifecycleScope.launchCatching {
            viewModel.getNotifications()
                .debounce(250)
                .collect {
                    setNotifications(it)
                    viewModel.markNotificationsAsSeen(it)
                }
        }

        lifecycleScope.launchCatching {
            viewModel.getNotificationCount()
                .collect {
                    binding.notificationsTitleBadge.text = it.toString()
                }
        }

        binding.notificationsRefreshLayout.setOnRefreshListener(this)
        lifecycleScope.launchCatching {
            viewModel.refreshNotifications()
        }

        binding.dismissAllButton.setOnClickListener {
            HapticFeedbackManager.tap(it)
            viewModel.dismissAllNotifications(notifications)
            setNotifications(emptyList())
        }
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

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            viewModel.refreshNotifications()
            binding.notificationsRefreshLayout.isRefreshing = false
        }
    }

    private fun setNotifications(notifications: List<Notification>) {
        this.notifications = notifications

        if (notifications.isEmpty()) {
            displayNoNotificationsView()
        } else {
            displayNotificationsListView(notifications)
        }
    }

    private fun displayNoNotificationsView() {
        binding.notificationItems.removeAllViewsInLayout()
        binding.notificationItems.showDividers = LinearLayout.SHOW_DIVIDER_NONE
        binding.notificationItems.addView(
            inflater?.inflate(
                R.layout.no_notifications,
                binding.notificationItems,
                false,
            ),
        )
        binding.progressView.isVisible = false
    }

    private fun displayNotificationsListView(notifications: List<Notification>) {
        binding.notificationItems.showDividers =
            LinearLayout.SHOW_DIVIDER_MIDDLE or LinearLayout.SHOW_DIVIDER_END

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val currentViews =
                mutableSetOf<View>().apply {
                    addAll(binding.notificationItems.children)
                }
            notifications.forEach {
                val item: View? =
                    when (it.type) {
                        Notification.Type.NEW_CHAT_MESSAGE.type -> createNewChatMessageNotification(it)
                        Notification.Type.NEW_STUFF.type -> createNewStuffNotification(it)
                        Notification.Type.UNALLOCATED_STATS_POINTS.type ->
                            createUnallocatedStatsNotification(
                                it,
                            )

                        Notification.Type.NEW_MYSTERY_ITEMS.type -> createMysteryItemsNotification(it)
                        Notification.Type.GROUP_TASK_NEEDS_WORK.type ->
                            createGroupTaskNeedsWorkNotification(
                                it,
                            )

                        Notification.Type.GROUP_TASK_APPROVED.type ->
                            createGroupTaskApprovedNotification(
                                it,
                            )

                        Notification.Type.GROUP_TASK_REQUIRES_APPROVAL.type ->
                            createGroupTaskNeedsApprovalNotification(
                                it,
                            )

                        Notification.Type.PARTY_INVITATION.type -> createPartyInvitationNotification(it)
                        Notification.Type.QUEST_INVITATION.type -> createQuestInvitationNotification(it)
                        Notification.Type.ITEM_RECEIVED.type -> createItemReceivedNotification(it)
                        Notification.Type.GUILD_INVITATION.type -> createGuildInvitationNotification(it)
                        else -> null
                    }

                item?.let { view ->
                    if (!currentViews.removeIf { it.tag == view.tag }) {
                        binding.notificationItems.addView(view)
                    }
                }
            }

            // Remove views that are no longer valid
            currentViews.forEach { binding.notificationItems.removeView(it) }

            lifecycleScope.launch {
                binding.progressView.isVisible = false
                delay(250)
                if (binding.notificationItems.visibility != View.VISIBLE) {
                    binding.notificationItems.fadeInAnimation(200)
                }
            }
        }
    }

    private fun removeNotificationAndRefresh(notification: Notification) {
        // Immediately remove notification for better user experience
        // (To avoid waiting for the server to respond for potential slower connections)
        this.notifications = this.notifications.filter { it.id != notification.id }

        if (notifications.isEmpty()) {
            displayNoNotificationsView()
        } else {
            displayNotificationsListView(notifications)
        }
    }

    private fun createNewChatMessageNotification(notification: Notification): View? {
        val data = notification.data as? NewChatMessageData
        val stringId =
            if (viewModel.isPartyMessage(data)) R.string.new_msg_party else R.string.new_msg_guild

        return createDismissableNotificationItem(
            notification,
            fromHtml(getString(stringId, data?.group?.name)),
        )
    }

    private fun createItemReceivedNotification(notification: Notification): View? {
        val data = notification.data as? ItemReceivedData
        return createDismissableNotificationItem(
            notification,
            fromHtml("<b>" + data?.title + "</b><br>" + data?.text),
            imageName = data?.icon,
        )
    }

    private var baileyNewsNotification: Notification? = null

    private suspend fun createNewStuffNotification(notification: Notification): View? =
        withContext(Dispatchers.IO) {
            var baileyNotification = notification
            val data = notification.data as? NewStuffData
            val text =
                if (data?.title != null) {
                    fromHtml("<b>" + getString(R.string.new_bailey_update) + "</b><br>" + data.title)
                } else {
                    baileyNotification =
                        baileyNewsNotification ?: userRepository.getNewsNotification() ?: notification
                    baileyNewsNotification = baileyNotification
                    val baileyNewsData = baileyNotification.data as? NewStuffData
                    fromHtml("<b>" + getString(R.string.new_bailey_update) + "</b><br>" + baileyNewsData?.title)
                }
            baileyNotification.id = notification.id

            return@withContext withContext(Dispatchers.Main) {
                createDismissableNotificationItem(
                    baileyNotification,
                    text,
                    R.drawable.notifications_bailey,
                )
            }
        }

    private fun createUnallocatedStatsNotification(notification: Notification): View? {
        val level = userLvl ?: return null
        return if (level >= 10) {
            val data = notification.data as? UnallocatedPointsData

            createDismissableNotificationItem(
                notification,
                fromHtml(getString(R.string.unallocated_stats_points, data?.points.toString())),
                R.drawable.notification_stat_sparkles,
            )
        } else {
            null
        }
    }

    private fun createMysteryItemsNotification(notification: Notification): View? {
        return createDismissableNotificationItem(
            notification,
            fromHtml(getString(R.string.new_subscriber_item)),
            R.drawable.notification_mystery_item,
        )
    }

    private fun createGroupTaskNeedsWorkNotification(notification: Notification): View? {
        val data = notification.data as? GroupTaskNeedsWorkData
        val message = convertGroupMessageHtml(data?.message ?: "")

        return createDismissableNotificationItem(
            notification,
            fromHtml(message),
            null,
            textColor = R.color.yellow_5,
        )
    }

    private fun createGroupTaskApprovedNotification(notification: Notification): View? {
        val data = notification.data as? GroupTaskApprovedData
        val message = convertGroupMessageHtml(data?.message ?: "")

        return createDismissableNotificationItem(
            notification,
            fromHtml(message),
            null,
            textColor = R.color.green_10,
        )
    }

    private fun createGroupTaskNeedsApprovalNotification(notification: Notification): View? {
        val data = notification.data as? GroupTaskRequiresApprovalData
        val message = convertGroupMessageHtml(data?.message ?: "")

        val item =
            createActionableNotificationItem(
                notification,
                fromHtml(message),
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
        imageName: String? = null,
        textColor: Int? = null,
    ): View? {
        val item = inflater?.inflate(R.layout.notification_item, binding.notificationItems, false)
        item?.tag = notification.id

        val container = item?.findViewById(R.id.notification_item) as? View
        container?.setOnClickListener {
            it.flash()
            HapticFeedbackManager.tap(it)
            val resultIntent = Intent()
            resultIntent.putExtra("notificationId", notification.id)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        val dismissButton = item?.findViewById(R.id.dismiss_button) as? ImageView
        dismissButton?.setOnClickListener {
            it.flash()
            HapticFeedbackManager.tap(it)
            removeNotificationAndRefresh(notification)
            viewModel.dismissNotification(notification)
        }

        val messageTextView = item?.findViewById(R.id.message_text) as? TextView
        messageTextView?.text = messageText

        if (imageResourceId != null) {
            val notificationImage = item?.findViewById(R.id.notification_image) as? ImageView
            notificationImage?.setImageResource(imageResourceId)
            notificationImage?.visibility = View.VISIBLE
        }

        if (imageName != null) {
            val notificationImage = item?.findViewById(R.id.notification_image) as? PixelArtView
            notificationImage?.loadImage(imageName)
            notificationImage?.visibility = View.VISIBLE
        }

        if (textColor != null) {
            messageTextView?.setTextColor(ContextCompat.getColor(this, textColor))
        }

        return item
    }

    private suspend fun createPartyInvitationNotification(notification: Notification): View? =
        withContext(ExceptionHandler.coroutine()) {
            val data = notification.data as? PartyInvitationData
            val inviterId = data?.invitation?.inviter
            if (inviterId != null) {
                val inviter = socialRepository.retrieveMember(inviterId, fromHall = false)
                return@withContext createActionableNotificationItem(
                    notification,
                    fromHtml(
                        getString(
                            R.string.invited_to_party_notification,
                            data.invitation?.name,
                            inviter?.formattedUsername,
                        ),
                    ),
                    openable = true,
                    inviterId,
                )
            } else {
                return@withContext null
            }
        }

    private suspend fun createGuildInvitationNotification(notification: Notification): View? =
        withContext(ExceptionHandler.coroutine()) {
            val data = notification.data as? GuildInvitationData
            val inviterId = data?.invitation?.inviter
            if (inviterId != null) {
                val inviter = socialRepository.retrieveMember(inviterId, fromHall = false)
                return@withContext createActionableNotificationItem(
                    notification,
                    fromHtml(
                        getString(
                            R.string.invited_to_guild_notification,
                            data.invitation?.name,
                            inviter?.formattedUsername,
                        ),
                    ),
                    openable = true,
                    inviterId,
                )
            } else {
                return@withContext null
            }
        }

    private fun createQuestInvitationNotification(notification: Notification): View? {
        val data = notification.data as? QuestInvitationData

        val view = createActionableNotificationItem(notification, "", true)

        // hide view until we have loaded quest data and populated the values
        view?.visibility = View.GONE

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val questContent =
                inventoryRepository.getQuestContent(data?.questKey ?: "").firstOrNull()
            if (questContent != null) {
                updateQuestInvitationView(view, questContent)
            }
        }

        return view
    }

    private fun updateQuestInvitationView(
        view: View?,
        questContent: QuestContent,
    ) {
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
        openable: Boolean = false,
        inviterId: String? = null,
    ): View? {
        val item =
            inflater?.inflate(
                R.layout.notification_item_actionable,
                binding.notificationItems,
                false,
            )
        item?.tag = notification.id

        if (openable) {
            val container = item?.findViewById(R.id.notification_item) as? View
            container?.setOnClickListener {
                it.flash()
                HapticFeedbackManager.tap(it)
                if (inviterId != null) {
                    FullProfileActivity.open(inviterId)
                } else {
                    val resultIntent = Intent()
                    resultIntent.putExtra("notificationId", notification.id)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }

        val acceptButton = item?.findViewById(R.id.accept_button) as? Button
        acceptButton?.setOnClickListener {
            HapticFeedbackManager.tap(it)
            removeNotificationAndRefresh(notification)
            viewModel.accept(notification.id)
        }

        val rejectButton = item?.findViewById(R.id.reject_button) as? Button
        rejectButton?.setOnClickListener {
            HapticFeedbackManager.tap(it)
            removeNotificationAndRefresh(notification)
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
