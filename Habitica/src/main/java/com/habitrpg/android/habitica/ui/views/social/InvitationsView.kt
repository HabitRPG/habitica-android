package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.shared.habitica.models.invitations.GenericInvitation

class InvitationsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var acceptCall: ((String) -> Unit)? = null
    var rejectCall: ((String) -> Unit)? = null

    init {
        orientation = VERTICAL
    }

    fun setInvitations(invitations: List<GenericInvitation>) {
        removeAllViews()
        for (invitation in invitations) {
            val view = inflate(R.layout.view_invitation, true)
            val textView = view.findViewById<TextView>(R.id.text_view)
            textView.text = context.getString(R.string.invitation_title, context.getString(R.string.someone), invitation.name)
            view.findViewById<Button>(R.id.accept_button).setOnClickListener {
                invitation.id?.let { it1 -> acceptCall?.invoke(it1) }
            }
            view.findViewById<Button>(R.id.reject_button).setOnClickListener {
                invitation.id?.let { it1 -> rejectCall?.invoke(it1) }
            }
        }
    }
}