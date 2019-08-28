package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class AchievementDialog(context: Context) : HabiticaAlertDialog(context) {

    private var iconView: SimpleDraweeView?
    private var titleView: TextView?
    private var descriptionView: TextView?

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_achievement_detail, null)
        iconView = view?.findViewById(R.id.icon_view)
        titleView = view?.findViewById(R.id.title_view)
        titleView?.visibility = View.VISIBLE
        descriptionView = view?.findViewById(R.id.description_view)
        setAdditionalContentView(view)
    }

    fun setType(type: String) {
        when (type) {
            Notification.Type.ACHIEVEMENT_PARTY_UP.type -> configure(R.string.partyUpTitle, R.string.partyUpDescription, "partyUp")
            Notification.Type.ACHIEVEMENT_PARTY_ON.type -> configure(R.string.partyOnTitle, R.string.partyOnDescription, "partyOn")
            Notification.Type.ACHIEVEMENT_BEAST_MASTER.type -> configure(R.string.beastMasterTitle, R.string.beastMasterDescription, "rat")
            Notification.Type.ACHIEVEMENT_MOUNT_MASTER.type -> configure(R.string.mountMasterTitle, R.string.mountMasterDescription, "wolf")
            Notification.Type.ACHIEVEMENT_TRIAD_BINGO.type -> configure(R.string.triadBingoTitle, R.string.triadBingoDescription, "triadbingo")
            Notification.Type.ACHIEVEMENT_GUILD_JOINED.type -> configure(R.string.joinedGuildTitle, R.string.joinedGuildDescription, "guild")
            Notification.Type.ACHIEVEMENT_CHALLENGE_JOINED.type -> configure(R.string.joinedChallengeTitle, R.string.joinedChallengeDescription, "challenge")
            Notification.Type.ACHIEVEMENT_INVITED_FRIEND.type -> configure(R.string.inviteFriendTitle, R.string.inviteFriendDescription, "friends")
        }
    }

    private fun configure(titleID: Int, descriptionID: Int, iconName: String) {
        titleView?.text = context.getString(titleID)
        descriptionView?.text = context.getString(descriptionID)
        DataBindingUtils.loadImage(iconView, "achievement-${iconName}2x")
        setTitle(R.string.achievement_title)
        addButton(R.string.onwards, true)
        addButton(R.string.view_achievements, isPrimary = false, isDestructive = false) { _, _ ->
            MainNavigationController.navigate(R.id.achievementsFragment)
        }
    }
}
