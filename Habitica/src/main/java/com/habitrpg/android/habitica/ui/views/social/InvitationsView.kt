package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ViewInvitationBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.invitations.GenericInvitation
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.setMarkdown
import kotlinx.coroutines.launch

class InvitationsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var acceptCall: ((String) -> Unit)? = null
    var rejectCall: ((String) -> Unit)? = null
    var getLeader: (suspend (String) -> Member?)? = null

    init {
        orientation = VERTICAL
    }

    fun setInvitations(invitations: List<GenericInvitation>) {
        removeAllViews()
        for (invitation in invitations) {
            val leaderID = invitation.inviter
            val binding = ViewInvitationBinding.inflate(context.layoutInflater, this, true)
            binding.groupleaderTextView.movementMethod = LinkMovementMethod.getInstance()

            findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                leaderID?.let {
                    val leader = getLeader?.invoke(it) ?: return@launch
                    binding.groupleaderAvatarView.setAvatar(leader)
                    binding.groupleaderTextView.setMarkdown(
                        context.getString(
                            R.string.invitation_title,
                            "[${leader.formattedUsername}](https://habitica.com/profile/$leaderID)",
                            invitation.name
                        )
                    )
                }
            }

            binding.root.setOnClickListener {
                leaderID?.let { id ->
                    val profileDirections = MainNavDirections.openProfileActivity(id)
                    MainNavigationController.navigate(profileDirections)
                }
            }

            binding.acceptButton.setOnClickListener {
                invitation.id?.let { it1 -> acceptCall?.invoke(it1) }
            }
            binding.rejectButton.setOnClickListener {
                invitation.id?.let { it1 -> rejectCall?.invoke(it1) }
            }
        }
    }
}
