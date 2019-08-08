package com.habitrpg.android.habitica.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LocalNotificationActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var apiClient: ApiClient

    private var user: User? = null
    private var groupID: String? = null
    private var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        HabiticaBaseApplication.userComponent?.inject(this)
        groupID = intent.extras?.getString("groupID")
        this.context = context
        handleLocalNotificationAction(intent.action)
    }

    private fun handleLocalNotificationAction(action: String?) {
        val notificationManager = this.context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancelAll()
        when (action) {
            context?.getString(R.string.accept_party_invite) -> {
                groupID?.let {
                    socialRepository.joinGroup(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.reject_party_invite) -> {
                groupID?.let {
                    socialRepository.rejectGroupInvite(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.accept_quest_invite) -> {
                socialRepository.acceptQuest(user).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            context?.getString(R.string.reject_quest_invite) -> {
                socialRepository.rejectQuest(user).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            context?.getString(R.string.accept_guild_invite) -> {
                groupID?.let {
                    socialRepository.joinGroup(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
            context?.getString(R.string.reject_guild_invite) -> {
                groupID?.let {
                    socialRepository.rejectGroupInvite(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
        }
    }
}
