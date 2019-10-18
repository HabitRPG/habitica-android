package com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder

import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.members.Member
import com.habitrpg.shared.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.StatsLanguages.getTranslatedClassName
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaProgressBar
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel


class GroupMemberViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), PopupMenu.OnMenuItemClickListener {

    private var currentUserID: String? = null
    private var leaderID: String? = null
    private val avatarView: AvatarView by bindView(R.id.avatarView)
    private val displayNameTextView: UsernameLabel by bindView(R.id.display_name_textview)
    private val sublineTextView: TextView by bindView(R.id.subline_textview)
    private val buffIconView: ImageView by bindView(R.id.buff_icon_view)
    private val classIconView: ImageView by bindView(R.id.class_icon_view)
    private val healthBar: HabiticaProgressBar by bindView(R.id.health_bar)
    private val experienceBar: HabiticaProgressBar by bindView(R.id.experience_bar)
    private val manaBar: HabiticaProgressBar by bindView(R.id.mana_bar)
    private val healthTextView: TextView by bindView(R.id.health_textview)
    private val experienceTextView: TextView by bindView(R.id.experience_textview)
    private val manaTextView: TextView by bindView(R.id.mana_textview)
    private val moreButton: ImageButton by bindView(R.id.more_button)
    //private val leaderTextView: TextView by bindView(R.id.leader_textview)

    var onClickEvent: (() -> Unit)? = null
    var sendMessageEvent: (() -> Unit)? = null
    var removeMemberEvent: (() -> Unit)? = null
    var transferOwnershipEvent: (() -> Unit)? = null

    init {
        buffIconView.setImageBitmap(HabiticaIconsHelper.imageOfBuffIcon())
        itemView.setOnClickListener { onClickEvent?.invoke() }
        moreButton.setOnClickListener { showOptionsPopup() }
    }

    private fun showOptionsPopup() {
        val popup = PopupMenu(itemView.context, moreButton)
        popup.setOnMenuItemClickListener(this)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.party_member_menu, popup.menu)
        popup.menu.findItem(R.id.transfer_ownership).isVisible = currentUserID == leaderID
        popup.menu.findItem(R.id.remove).isVisible = currentUserID == leaderID
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.send_message -> { sendMessageEvent?.invoke() }
            R.id.transfer_ownership -> { transferOwnershipEvent?.invoke() }
            R.id.remove -> { removeMemberEvent?.invoke() }
        }
        return true
    }

    fun bind(user: Member, leaderID: String?, userID: String?) {
        avatarView.setAvatar(user)
        this.leaderID = leaderID
        this.currentUserID = userID

        if (user.id == userID) {
            moreButton.visibility = View.GONE
        } else {
            moreButton.visibility = View.VISIBLE
        }

        user.stats?.let {
            healthBar.set(it.hp ?: 0.0, it.maxHealth?.toDouble() ?: 50.0)
            healthTextView.text = "${it.hp?.toInt()} / ${it.maxHealth}"
            experienceBar.set(it.exp ?: 0.0, it.toNextLevel?.toDouble() ?: 0.0)
            experienceTextView.text = "${it.exp?.toInt()} / ${it.toNextLevel}"
            manaBar.set(it.mp ?: 0.0, it.maxMP?.toDouble() ?: 0.0)
            manaTextView.text = "${it.mp?.toInt()} / ${it.maxMP}"
        }
        displayNameTextView.username = user.profile?.name
        displayNameTextView.tier = user.contributor?.level ?: 0

        if (user.hasClass()) {
            sublineTextView.text = itemView.context.getString(R.string.user_level_with_class, user.stats?.lvl, getTranslatedClassName(user.stats?, itemView.context))
        } else {
            sublineTextView.text = itemView.context.getString(R.string.user_level, user.stats?.lvl)
        }

        if (user.stats?.isBuffed == true) {
            buffIconView.visibility = View.VISIBLE
        } else {
            buffIconView.visibility = View.GONE
        }

        classIconView.visibility = View.VISIBLE
        when (user.stats?.habitClass) {
            Stats.HEALER -> {
                classIconView.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            }
            Stats.WARRIOR -> {
                classIconView.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
            }
            Stats.ROGUE -> {
                classIconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            }
            Stats.MAGE -> {
                classIconView.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            }
            else -> {
                classIconView.visibility = View.INVISIBLE
            }
        }

        itemView.isClickable = true
        //leaderTextView.visibility = if (user.id == leaderID) View.VISIBLE else View.GONE
    }
}
