package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.habitrpg.android.habitica.databinding.ViewInvitationBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.invitations.GenericInvitation

class InvitationsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var acceptCall: ((String) -> Unit)? = null
    var rejectCall: ((String) -> Unit)? = null
    var setLeader: ((String) -> Unit)? = null
    var leaderID: String? = null
    var groupName: String? = null

    init {
        orientation = VERTICAL
    }

    fun setInvitations(invitations: List<GenericInvitation>) {
        removeAllViews()
        for (invitation in invitations) {
            leaderID = invitation.inviter
            groupName = invitation.name
            val binding = ViewInvitationBinding.inflate(context.layoutInflater, this, true)

            leaderID?.let {
                setLeader?.invoke(it)
                invalidate()
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
