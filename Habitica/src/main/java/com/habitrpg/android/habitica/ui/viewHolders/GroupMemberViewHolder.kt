package com.habitrpg.android.habitica.ui.viewHolders

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PartyMemberBinding
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.getTranslatedClassName

class GroupMemberViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), PopupMenu.OnMenuItemClickListener {
    private val binding = PartyMemberBinding.bind(itemView)
    private var currentUserID: String? = null
    private var leaderID: String? = null

    var onClickEvent: (() -> Unit)? = null
    var sendMessageEvent: (() -> Unit)? = null
    var removeMemberEvent: (() -> Unit)? = null
    var transferOwnershipEvent: (() -> Unit)? = null

    init {
        binding.buffIconView.setImageBitmap(HabiticaIconsHelper.imageOfBuffIcon())
        itemView.setOnClickListener { onClickEvent?.invoke() }
        binding.moreButton.setOnClickListener { showOptionsPopup() }
    }

    private fun showOptionsPopup() {
        val popup = PopupMenu(itemView.context, binding.moreButton)
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

    @SuppressLint("SetTextI18n")
    fun bind(user: Member, leaderID: String?, userID: String?) {
        binding.avatarView.setAvatar(user)
        this.leaderID = leaderID
        this.currentUserID = userID

        if (user.id == userID) {
            binding.youPill.visibility = View.VISIBLE
            binding.moreButton.visibility = View.GONE
        } else {
            binding.youPill.visibility = View.GONE
            binding.moreButton.visibility = View.VISIBLE
        }

        user.stats?.let {
            binding.healthBar.set(it.hp ?: 0.0, it.maxHealth?.toDouble() ?: 50.0)
            binding.healthTextview.text = "${it.hp?.toInt()} / ${it.maxHealth}"
            binding.experienceBar.set(it.exp ?: 0.0, it.toNextLevel?.toDouble() ?: 0.0)
            binding.experienceTextview.text = "${it.exp?.toInt()} / ${it.toNextLevel}"
            binding.manaBar.set(it.mp ?: 0.0, it.maxMP?.toDouble() ?: 0.0)
            binding.manaTextview.text = "${it.mp?.toInt()} / ${it.maxMP}"
        }
        binding.displayNameTextview.username = user.profile?.name
        binding.displayNameTextview.tier = user.contributor?.level ?: 0

        if (user.hasClass) {
            binding.sublineTextview.text = itemView.context.getString(R.string.user_level_with_class, user.stats?.lvl, getTranslatedClassName(itemView.context.resources, user.stats?.habitClass))
        } else {
            binding.sublineTextview.text = itemView.context.getString(R.string.user_level, user.stats?.lvl)
        }

        if (user.stats?.isBuffed == true) {
            binding.buffIconView.visibility = View.VISIBLE
        } else {
            binding.buffIconView.visibility = View.GONE
        }

        binding.classIconView.visibility = View.VISIBLE
        when (user.stats?.habitClass) {
            Stats.HEALER -> {
                binding.classIconView.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            }
            Stats.WARRIOR -> {
                binding.classIconView.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
            }
            Stats.ROGUE -> {
                binding.classIconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            }
            Stats.MAGE -> {
                binding.classIconView.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            }
            else -> {
                binding.classIconView.visibility = View.INVISIBLE
            }
        }

        itemView.isClickable = true
    }
}
