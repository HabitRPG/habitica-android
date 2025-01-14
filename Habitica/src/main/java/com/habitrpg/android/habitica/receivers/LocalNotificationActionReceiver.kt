package com.habitrpg.android.habitica.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
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

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
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
                    MainScope().launch(ExceptionHandler.coroutine()) {
                        socialRepository.joinGroup(it)
                    }
                }
            }

            context?.getString(R.string.reject_party_invite) -> {
                groupID?.let {
                    MainScope().launchCatching {
                        socialRepository.rejectGroupInvite(it)
                    }
                }
            }

            context?.getString(R.string.accept_quest_invite) -> {
                MainScope().launchCatching {
                    socialRepository.acceptQuest(user)
                }
            }

            context?.getString(R.string.reject_quest_invite) -> {
                MainScope().launchCatching {
                    socialRepository.rejectQuest(user)
                }
            }

            context?.getString(R.string.accept_guild_invite) -> {
                groupID?.let {
                    MainScope().launch(ExceptionHandler.coroutine()) {
                        socialRepository.joinGroup(it)
                    }
                }
            }

            context?.getString(R.string.reject_guild_invite) -> {
                groupID?.let {
                    MainScope().launchCatching {
                        socialRepository.rejectGroupInvite(it)
                    }
                }
            }

            context?.getString(R.string.group_message_reply) -> {
                groupID?.let {
                    getMessageText(context?.getString(R.string.group_message_reply))?.let { message ->
                        MainScope().launchCatching {
                            socialRepository.postGroupChat(it, message)
                            context?.let { c ->
                                NotificationManagerCompat.from(c).cancel(it.hashCode())
                            }
                        }
                    }
                }
            }

            context?.getString(R.string.inbox_message_reply) -> {
                senderID?.let {
                    getMessageText(context?.getString(R.string.inbox_message_reply))?.let { message ->
                        MainScope().launch(ExceptionHandler.coroutine()) {
                            socialRepository.postPrivateMessage(it, message)
                        }
                    }
                }
            }

            context?.getString(R.string.complete_task_action) -> {
                taskID?.let {
                    MainScope().launch(ExceptionHandler.coroutine()) {
                        taskRepository.taskChecked(null, it, up = true, force = false) {
                            val pair =
                                NotifyUserUseCase.getNotificationAndAddStatsToUserAsText(
                                    it.experienceDelta,
                                    it.healthDelta,
                                    it.goldDelta,
                                    it.manaDelta
                                )
                            showToast(pair.first)
                        }
                    }
                }
            }
        }
    }

    private fun showToast(text: Spannable) {
        val toast = Toast.makeText(context, text, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun getMessageText(key: String?): String? {
        return intent?.let {
            RemoteInput.getResultsFromIntent(it)?.getCharSequence(key)?.toString()
        }
    }
}
