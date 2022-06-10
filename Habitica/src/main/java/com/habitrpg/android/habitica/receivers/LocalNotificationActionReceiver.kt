package com.habitrpg.android.habitica.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.user.User
import javax.inject.Inject

class LocalNotificationActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var apiClient: ApiClient

    private var user: User? = null
    private val groupID: String?
        get() = intent?.extras?.getString("groupID")
    private val senderID: String?
        get() = intent?.extras?.getString("senderID")
    private val taskID: String?
        get() = intent?.extras?.getString("taskID")
    private var context: Context? = null
    private var intent: Intent? = null

    override fun onReceive(context: Context, intent: Intent) {
        HabiticaBaseApplication.userComponent?.inject(this)
        this.intent = intent
        this.context = context
        handleLocalNotificationAction(intent.action)
    }

    private fun handleLocalNotificationAction(action: String?) {
        val notificationManager =
            this.context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(intent?.extras?.getInt("NOTIFICATION_ID") ?: -1)
        when (action) {
            context?.getString(R.string.accept_party_invite) -> {
                groupID?.let {
                    socialRepository.joinGroup(it).subscribe({ }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.reject_party_invite) -> {
                groupID?.let {
                    socialRepository.rejectGroupInvite(it)
                        .subscribe({ }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.accept_quest_invite) -> {
                socialRepository.acceptQuest(user).subscribe({ }, RxErrorHandler.handleEmptyError())
            }
            context?.getString(R.string.reject_quest_invite) -> {
                socialRepository.rejectQuest(user).subscribe({ }, RxErrorHandler.handleEmptyError())
            }
            context?.getString(R.string.accept_guild_invite) -> {
                groupID?.let {
                    socialRepository.joinGroup(it).subscribe({ }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.reject_guild_invite) -> {
                groupID?.let {
                    socialRepository.rejectGroupInvite(it)
                        .subscribe({ }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.group_message_reply) -> {
                groupID?.let {
                    getMessageText(context?.getString(R.string.group_message_reply))?.let { message ->
                        socialRepository.postGroupChat(it, message).subscribe(
                            {
                                context?.let { c ->
                                    NotificationManagerCompat.from(c).cancel(it.hashCode())
                                }
                            },
                            RxErrorHandler.handleEmptyError()
                        )
                    }
                }
            }
            context?.getString(R.string.inbox_message_reply) -> {
                senderID?.let {
                    getMessageText(context?.getString(R.string.inbox_message_reply))?.let { message ->
                        socialRepository.postPrivateMessage(it, message)
                            .subscribe({ }, RxErrorHandler.handleEmptyError())
                    }
                }
            }
            context?.getString(R.string.complete_task_action) -> {
                taskID?.let {
                    taskRepository.taskChecked(null, it, up = true, force = false) {
                    }.subscribe({
                        val pair = NotifyUserUseCase.getNotificationAndAddStatsToUserAsText(
                            it?.experienceDelta,
                            it?.healthDelta,
                            it?.goldDelta,
                            it?.manaDelta
                        )
                        showToast(pair.first)
                    }, RxErrorHandler.handleEmptyError())
                }
            }
        }
    }

    private fun showToast(text: Spannable) {
        val toast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun getMessageText(key: String?): String? {
        return intent?.let { RemoteInput.getResultsFromIntent(it)?.getCharSequence(key)?.toString() }
    }
}
