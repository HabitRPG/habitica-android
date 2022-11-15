package com.habitrpg.android.habitica.ui.adapter.social

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerIntroViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerMessageViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject

class InboxAdapter(private var user: User?, private var replyToUser: Member?) : PagedListAdapter<ChatMessage, ChatRecyclerViewHolder>(DIFF_CALLBACK) {
    private val FIRST_MESSAGE = 0
    private val NORMAL_MESSAGE = 1

    private var expandedMessageId: String? = null
    private val userLabelClickEvents = PublishSubject.create<String>()
    private val deleteMessageEvents = PublishSubject.create<ChatMessage>()
    private val flagMessageEvents = PublishSubject.create<ChatMessage>()
    private val replyMessageEvents = PublishSubject.create<String>()
    private val copyMessageEvents = PublishSubject.create<ChatMessage>()

    private fun isPositionIntroMessage(position: Int): Boolean {
        return (position == super.getItemCount() - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionIntroMessage(position)) FIRST_MESSAGE else NORMAL_MESSAGE
    }

    override fun getItemId(position: Int): Long {
        return if (isPositionIntroMessage(position)) -1 else super.getItemId(position)
    }

    override fun getItem(position: Int): ChatMessage? {
        return if (isPositionIntroMessage(position)) ChatMessage() else super.getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecyclerViewHolder {
        return if (viewType == FIRST_MESSAGE) ChatRecyclerIntroViewHolder(parent.inflate(R.layout.tavern_chat_intro_item), replyToUser?.id ?: "")
        else ChatRecyclerMessageViewHolder(parent.inflate(R.layout.chat_item), user?.id ?: "", false)
    }

    override fun onBindViewHolder(holder: ChatRecyclerViewHolder, position: Int) {
        val firstMessage: Boolean = getItemViewType(position) == FIRST_MESSAGE
        if (firstMessage) {
            val introHolder = holder as ChatRecyclerIntroViewHolder
            introHolder.bind(replyToUser)
            introHolder.onOpenProfile = { userLabelClickEvents.onNext(it) }
        } else {
            val message: ChatMessage = getItem(position) ?: return
            val messageHolder = holder as ChatRecyclerMessageViewHolder
            messageHolder.bind(
                message,
                user?.id ?: "",
                user,
                expandedMessageId == message.id
            )
            messageHolder.onShouldExpand = { expandMessage(message.id, position) }
            messageHolder.onOpenProfile = { userLabelClickEvents.onNext(it) }
            messageHolder.onReply = { replyMessageEvents.onNext(it) }
            messageHolder.onCopyMessage = { copyMessageEvents.onNext(it) }
            messageHolder.onFlagMessage = { flagMessageEvents.onNext(it) }
            messageHolder.onDeleteMessage = { deleteMessageEvents.onNext(it) }
        }
    }

    fun getUserLabelClickFlowable(): Flowable<String> {
        return userLabelClickEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getFlagMessageClickFlowable(): Flowable<ChatMessage> {
        return flagMessageEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getDeleteMessageFlowable(): Flowable<ChatMessage> {
        return deleteMessageEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getCopyMessageFlowable(): Flowable<ChatMessage> {
        return copyMessageEvents.toFlowable(BackpressureStrategy.DROP)
    }

    private fun expandMessage(id: String, position: Int) {
        if (isPositionIntroMessage(position))
            return
        expandedMessageId = if (expandedMessageId == id) {
            null
        } else {
            id
        }
        notifyItemChanged(position)
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ChatMessage>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                oldConcert: ChatMessage,
                newConcert: ChatMessage
            ) = oldConcert.id == newConcert.id

            override fun areContentsTheSame(
                oldConcert: ChatMessage,
                newConcert: ChatMessage
            ) = oldConcert.text == newConcert.text
        }
    }
}
