package com.habitrpg.android.habitica.ui.adapter.social

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class InboxAdapter(private var user: User?) : PagedListAdapter<ChatMessage, ChatRecyclerViewHolder>(DIFF_CALLBACK) {
    private var expandedMessageId: String? = null

    private val likeMessageEvents = PublishSubject.create<ChatMessage>()
    private val userLabelClickEvents = PublishSubject.create<String>()
    private val deleteMessageEvents = PublishSubject.create<ChatMessage>()
    private val flagMessageEvents = PublishSubject.create<ChatMessage>()
    private val replyMessageEvents = PublishSubject.create<String>()
    private val copyMessageEvents = PublishSubject.create<ChatMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecyclerViewHolder {
        return ChatRecyclerViewHolder(parent.inflate(R.layout.chat_item), user?.id ?: "", false)
    }

    override fun onBindViewHolder(holder: ChatRecyclerViewHolder, position: Int) {
        val message = getItem(position) ?: return

        holder.bind(message,
                user?.id ?: "",
                user,
                expandedMessageId == message.id)
        holder.onShouldExpand = { expandMessage(message.id, position) }
        holder.onLikeMessage = { likeMessageEvents.onNext(it) }
        holder.onOpenProfile = { userLabelClickEvents.onNext(it) }
        holder.onReply = { replyMessageEvents.onNext(it) }
        holder.onCopyMessage = { copyMessageEvents.onNext(it) }
        holder.onFlagMessage = { flagMessageEvents.onNext(it) }
        holder.onDeleteMessage = { deleteMessageEvents.onNext(it) }
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
            override fun areItemsTheSame(oldConcert: ChatMessage,
                                         newConcert: ChatMessage) = oldConcert.id == newConcert.id

            override fun areContentsTheSame(oldConcert: ChatMessage,
                                            newConcert: ChatMessage) = oldConcert == newConcert
        }
    }
}