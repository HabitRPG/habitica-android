package com.habitrpg.android.habitica.ui.adapter.social

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensionsCommon.inflate
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerIntroViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerMessageViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerViewHolder

class InboxAdapter(private var user: User?) :
    PagingDataAdapter<ChatMessage, ChatRecyclerViewHolder>(DIFF_CALLBACK) {
    internal var replyToUser: Member? = null

    private var expandedMessageId: String? = null
    var onOpenProfile: ((String) -> Unit)? = null
    var onDeleteMessage: ((ChatMessage) -> Unit)? = null
    var onFlagMessage: ((ChatMessage) -> Unit)? = null
    var onReply: ((String) -> Unit)? = null
    var onCopyMessage: ((ChatMessage) -> Unit)? = null

    private fun isPositionIntroMessage(position: Int): Boolean {
        return (position == super.getItemCount() - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionIntroMessage(position)) FIRST_MESSAGE else NORMAL_MESSAGE
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ChatRecyclerViewHolder {
        return if (viewType == FIRST_MESSAGE) {
            ChatRecyclerIntroViewHolder(
                parent.inflate(R.layout.tavern_chat_intro_item),
                replyToUser?.id ?: "",
            )
        } else {
            ChatRecyclerMessageViewHolder(parent.inflate(R.layout.chat_item), user?.id ?: "", false)
        }
    }

    override fun onBindViewHolder(
        holder: ChatRecyclerViewHolder,
        position: Int,
    ) {
        val firstMessage: Boolean = getItemViewType(position) == FIRST_MESSAGE
        if (firstMessage) {
            val introHolder = holder as ChatRecyclerIntroViewHolder
            introHolder.bind(replyToUser)
            introHolder.onOpenProfile = onOpenProfile
        } else {
            val message: ChatMessage = getItem(position) ?: return
            val messageHolder = holder as ChatRecyclerMessageViewHolder
            messageHolder.bind(
                message,
                user?.id ?: "",
                user,
                expandedMessageId == message.id,
            )
            messageHolder.onShouldExpand = { expandMessage(message.id, position) }
            messageHolder.onOpenProfile = onOpenProfile
            messageHolder.onReply = onReply
            messageHolder.onCopyMessage = onCopyMessage
            messageHolder.onFlagMessage = onFlagMessage
            messageHolder.onDeleteMessage = onDeleteMessage
        }
    }

    private fun expandMessage(
        id: String,
        position: Int,
    ) {
        if (isPositionIntroMessage(position)) {
            return
        }
        expandedMessageId =
            if (expandedMessageId == id) {
                null
            } else {
                id
            }
        notifyItemChanged(position)
    }

    companion object {
        private const val FIRST_MESSAGE = 0
        private const val NORMAL_MESSAGE = 1

        private val DIFF_CALLBACK =
            object :
                DiffUtil.ItemCallback<ChatMessage>() {
                // Concert details may have changed if reloaded from the database,
                // but ID is fixed.
                override fun areItemsTheSame(
                    oldConcert: ChatMessage,
                    newConcert: ChatMessage,
                ) = oldConcert.id == newConcert.id

                override fun areContentsTheSame(
                    oldConcert: ChatMessage,
                    newConcert: ChatMessage,
                ) = oldConcert.text == newConcert.text
            }
    }
}
