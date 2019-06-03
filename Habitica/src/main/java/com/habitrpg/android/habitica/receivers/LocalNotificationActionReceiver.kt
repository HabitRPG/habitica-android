package com.habitrpg.android.habitica.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
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
    private var action: String? = null
    private var resources: Resources? = null
    private var intent: Intent? = null
    private var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        HabiticaBaseApplication.userComponent?.inject(this)
        this.resources = context.resources

        this.action = intent.action
        this.intent = intent
        this.context = context

        this.userRepository.getUser().firstElement().subscribe(Consumer { this.onUserReceived(it) }, RxErrorHandler.handleEmptyError())
    }

    fun onUserReceived(user: User) {
        this.user = user
        this.handleLocalNotificationAction(action)
        userRepository.close()
    }

    private fun handleLocalNotificationAction(action: String?) {
        val notificationManager = this.context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancelAll()

        //@TODO: This is a good place for a factory and event emitter pattern
        when (action) {
            this.resources?.getString(R.string.accept_party_invite) -> {
                val partyID = this.user?.invitations?.party?.id ?: return
                socialRepository.joinGroup(partyID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            this.resources?.getString(R.string.reject_party_invite) -> {
                val partyID = this.user?.invitations?.party?.id ?: return
                socialRepository.rejectGroupInvite(partyID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            this.resources?.getString(R.string.accept_quest_invite) -> {
                val partyID = this.user?.party?.id ?: return
                socialRepository.acceptQuest(user, partyID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            this.resources?.getString(R.string.reject_quest_invite) -> {
                val partyID = this.user?.party?.id ?: return
                socialRepository.rejectQuest(user, partyID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            this.resources?.getString(R.string.accept_guild_invite) -> {
                val guildID = intent?.extras?.getString("groupID") ?: return
                socialRepository.joinGroup(guildID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
            this.resources?.getString(R.string.reject_guild_invite) -> {
                val guildID = intent?.extras?.getString("groupID") ?: return
                socialRepository.rejectGroupInvite(guildID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
        }
    }
}
