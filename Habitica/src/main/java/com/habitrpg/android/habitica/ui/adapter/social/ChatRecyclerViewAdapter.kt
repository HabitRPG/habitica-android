package com.habitrpg.android.habitica.ui.adapter.social

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SystemChatMessageBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.adapter.DiffCallback
import com.habitrpg.android.habitica.ui.viewHolders.ChatRecyclerMessageViewHolder

class ChatDiffCallback(oldList: List<BaseMainObject>, newList: List<BaseMainObject>) :
    DiffCallback<ChatMessage>(oldList, newList) {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].primaryIdentifier == newList[newItemPosition].primaryIdentifier
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition] as ChatMessage
        val newItem = newList[newItemPosition] as ChatMessage
        Log.d("Compare", "${oldItem.id}-${oldItem.likeCount} , ${newItem.id}-${newItem.likeCount}")
        return oldItem.likeCount == newItem.likeCount && oldItem.id == newItem.id
    }
}

class ChatRecyclerViewAdapter(user: User?, private val isTavern: Boolean) : BaseRecyclerViewAdapter<ChatMessage, RecyclerView.ViewHolder>() {
    internal var user = user
        set(value) {
            field = value
            uuid = user?.id ?: ""
        }
    private var uuid: String = ""
    private var expandedMessageId: String? = null

    var onMessageLike: ((ChatMessage) -> Unit)? = null
    var onOpenProfile: ((String) -> Unit)? = null
    var onDeleteMessage: ((ChatMessage) -> Unit)? = null
    var onFlagMessage: ((ChatMessage) -> Unit)? = null
    var onReply: ((String) -> Unit)? = null
    var onCopyMessage: ((ChatMessage) -> Unit)? = null

    override fun getDiffCallback(
        oldList: List<ChatMessage>,
        newList: List<ChatMessage>
    ): DiffCallback<ChatMessage> {
        return ChatDiffCallback(oldList, newList)
    }

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
            val message = data[position]
            sysChatHolder.bind(
                message,
                expandedMessageId == data[position].id
            )
            sysChatHolder.onShouldExpand = { expandMessage(message, position) }
        } else {
            val chatHolder = holder as? ChatRecyclerMessageViewHolder ?: return
            val message = data[position]
            chatHolder.bind(
                message,
                uuid,
                user,
                expandedMessageId == message.id
            )
            chatHolder.onShouldExpand = { expandMessage(message, position) }
            chatHolder.onLikeMessage = onMessageLike
            chatHolder.onOpenProfile = onOpenProfile
            chatHolder.onReply = onReply
            chatHolder.onCopyMessage = onCopyMessage
            chatHolder.onFlagMessage = onFlagMessage
            chatHolder.onDeleteMessage = onDeleteMessage
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (data.size <= position) return 0
        return if (data[position].isSystemMessage) 0 else 1
    }

    private fun expandMessage(message: ChatMessage, position: Int?) {
        expandedMessageId = if (expandedMessageId == message.id) {
            null
        } else {
            message.id
        }
        notifyItemChanged(position ?: data.indexOf(message))
    }
}

class SystemChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dateTime = java.text.SimpleDateFormat.getDateTimeInstance()
    val binding = SystemChatMessageBinding.bind(itemView)

    var onShouldExpand: (() -> Unit)? = null

    init {
        binding.textView.setOnClickListener {
            onShouldExpand?.invoke()
        }
    }

    fun bind(chatMessage: ChatMessage?, isExpanded: Boolean) {
        binding.textView.text = chatMessage?.text?.removePrefix("`")?.removeSuffix("`")
        binding.systemMessageTimestamp.text = chatMessage?.timestamp?.let { java.util.Date(it) }
            ?.let { dateTime.format(it) }
        if (isExpanded) {
            binding.systemMessageTimestamp.visibility = View.VISIBLE
        } else {
            binding.systemMessageTimestamp.visibility = View.GONE
        }
    }
}
