package com.habitrpg.android.habitica.ui.adapter.social

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ChatItemBinding
import com.habitrpg.android.habitica.databinding.SystemChatMessageBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerMessageViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject

class ChatRecyclerViewAdapter(user: User?, private val isTavern: Boolean) : BaseRecyclerViewAdapter<ChatMessage, RecyclerView.ViewHolder>() {
    internal var user = user
    set(value) {
        field = value
        uuid = user?.id ?: ""
    }
    private var uuid: String = ""
    private var expandedMessageId: String? = null

    private val likeMessageEvents = PublishSubject.create<ChatMessage>()
    private val userLabelClickEvents = PublishSubject.create<String>()
    private val deleteMessageEvents = PublishSubject.create<ChatMessage>()
    private val flagMessageEvents = PublishSubject.create<ChatMessage>()
    private val replyMessageEvents = PublishSubject.create<String>()
    private val copyMessageEvents = PublishSubject.create<ChatMessage>()

    init {
        this.uuid = user?.id ?: ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            SystemChatMessageViewHolder(parent.inflate(R.layout.system_chat_message))
        } else {
            ChatRecyclerMessageViewHolder(parent.inflate(R.layout.chat_item), uuid, isTavern)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (data[position].isSystemMessage) {
            val sysChatHolder = holder as? SystemChatMessageViewHolder ?: return
            sysChatHolder.bind(data[position],
                    expandedMessageId == data[position].id)
            sysChatHolder.onShouldExpand = { expandMessage(data[position].id, position)}
        } else {
            val chatHolder = holder as? ChatRecyclerMessageViewHolder ?: return
            val message = data[position]
            chatHolder.bind(message,
                    uuid,
                    user,
                    expandedMessageId == message.id)
            chatHolder.onShouldExpand = { expandMessage(message.id, position) }
            chatHolder.onLikeMessage = { likeMessageEvents.onNext(it) }
            chatHolder.onOpenProfile = { userLabelClickEvents.onNext(it) }
            chatHolder.onReply = { replyMessageEvents.onNext(it) }
            chatHolder.onCopyMessage = { copyMessageEvents.onNext(it) }
            chatHolder.onFlagMessage = { flagMessageEvents.onNext(it) }
            chatHolder.onDeleteMessage = { deleteMessageEvents.onNext(it) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isSystemMessage) 0 else 1
    }

    fun getLikeMessageFlowable(): Flowable<ChatMessage> {
        return likeMessageEvents.toFlowable(BackpressureStrategy.DROP)
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

    fun getReplyMessageEvents(): Flowable<String> {
        return replyMessageEvents.toFlowable(BackpressureStrategy.DROP)
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
}

class SystemChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textView: TextView = itemView.findViewById(R.id.text_view)
    private val timestamp: TextView = itemView.findViewById(R.id.system_message_timestamp)
    private val dateTime = java.text.SimpleDateFormat("MMM dd, hh:mm aaa")
    val binding = SystemChatMessageBinding.bind(itemView)

    var onShouldExpand: (() -> Unit)? = null

    init{
        textView.setOnClickListener {
            onShouldExpand?.invoke()
        }
    }

    fun bind(chatMessage: ChatMessage?, isExpanded: Boolean) {
        textView.text = chatMessage?.text?.removePrefix("`")?.removeSuffix("`")
        timestamp.text = dateTime.format(chatMessage?.timestamp?.let { java.util.Date(it) })
        if(isExpanded) {
            binding.systemMessageTimestamp.visibility = View.VISIBLE
        } else {
            binding.systemMessageTimestamp.visibility = View.GONE
        }
    }

}