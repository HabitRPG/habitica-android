package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.shared.habitica.models.invitations.GenericInvitation

class InvitationsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var acceptCall: ((String) -> Unit)? = null
    var rejectCall: ((String) -> Unit)? = null
    var setLeader: ((String) -> Unit)? = null
    val avatarView: AvatarView by bindView(R.id.groupleader_avatar_view)
    val textView: TextView by bindView(R.id.groupleader_text_view)
    var leaderID: String? = null
    var groupName: String? = null
    var leaderName: String? = null
    val view = inflate(R.layout.view_invitation, true)

    init {
        orientation = VERTICAL
    }

    fun setInvitations(invitations: List<GenericInvitation>) {
        removeAllViews()
        for (invitation in invitations) {
            leaderID = invitation.inviter
            groupName = invitation.name
            val view = inflate(R.layout.view_invitation, true)

            leaderID?.let {
                setLeader?.invoke(it)
                invalidate()
            }

            view.findViewById<Button>(R.id.accept_button).setOnClickListener {
                invitation.id?.let { it1 -> acceptCall?.invoke(it1) }
            }
            view.findViewById<Button>(R.id.reject_button).setOnClickListener {
                invitation.id?.let { it1 -> rejectCall?.invoke(it1) }
            }
        }
    }
}