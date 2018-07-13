package com.habitrpg.android.habitica.ui.adapter.social

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import net.pherth.android.emoji_library.EmojiTextView

class ChatRecyclerViewAdapter(data: OrderedRealmCollection<ChatMessage>?, autoUpdate: Boolean, private val user: User?, private val isTavern: Boolean) : RealmRecyclerViewAdapter<ChatMessage, ChatRecyclerViewAdapter.ChatRecyclerViewHolder>(data, autoUpdate) {
    private var uuid: String = ""
    private var sendingUser: User? = null
    private var expandedMessageId: String? = null

    private val likeMessageEvents = PublishSubject.create<ChatMessage>()
    private val userLabelClickEvents = PublishSubject.create<String>()
    private val deleteMessageEvents = PublishSubject.create<ChatMessage>()
    private val flagMessageEvents = PublishSubject.create<ChatMessage>()
    private val replyMessageEvents = PublishSubject.create<String>()
    private val copyMessageEvents = PublishSubject.create<ChatMessage>()

    init {
        if (user != null) this.uuid = user.id
    }

    fun setSendingUser(user: User?) {
        this.sendingUser = user
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecyclerViewHolder {
        return ChatRecyclerViewHolder(parent.inflate(R.layout.tavern_chat_item), uuid, isTavern)
    }

    override fun onBindViewHolder(holder: ChatRecyclerViewHolder, position: Int) {
        data.notNull { holder.bind(it[position]) }
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


    inner class ChatRecyclerViewHolder(itemView: View, private val userId: String, private val isTavern: Boolean) : RecyclerView.ViewHolder(itemView) {

        private val avatarView: AvatarView by bindView(R.id.avatar_view)
        private val userLabel: UsernameLabel by bindView(R.id.user_label)
        private val messageText: EmojiTextView by bindView(R.id.message_text)
        private val agoLabel: TextView by bindView(R.id.ago_label)
        private val likeBackground: LinearLayout by bindView(R.id.like_background_layout)
        private val tvLikes: TextView by bindView(R.id.tvLikes)
        private val buttonsWrapper: LinearLayout by bindView(R.id.buttons_wrapper)
        private val replyButton: Button by bindView(R.id.reply_button)
        private val copyButton: Button by bindView(R.id.copy_button)
        private val reportButton: Button by bindView(R.id.report_button)
        private val deleteButton: Button by bindView(R.id.delete_button)

        val context: Context = itemView.context
        val res: Resources = itemView.resources
        private var chatMessage: ChatMessage? = null

        init {
            itemView.setOnClickListener {
                expandedMessageId = if (expandedMessageId == chatMessage?.id) {
                    null
                } else {
                    chatMessage?.id
                }
                notifyItemChanged(adapterPosition)
            }
            tvLikes.setOnClickListener { chatMessage.notNull { likeMessageEvents.onNext(it) } }
            userLabel.setOnClickListener { chatMessage?.uuid.notNull {userLabelClickEvents.onNext(it) } }
            replyButton.setOnClickListener { chatMessage?.text.notNull { replyMessageEvents.onNext(it) } }
            copyButton.setOnClickListener { chatMessage.notNull { copyMessageEvents.onNext(it) } }
            reportButton.setOnClickListener { chatMessage.notNull { flagMessageEvents.onNext(it) } }
            deleteButton.setOnClickListener { chatMessage.notNull { deleteMessageEvents.onNext(it) } }
        }

        fun bind(msg: ChatMessage) {
            chatMessage = msg

            setLikeProperties()

            if (msg.sent != null && msg.sent == "true" && sendingUser != null) {
                userLabel.tier = sendingUser?.contributor?.level ?: 0
            } else {
                userLabel.tier = msg.contributor?.level ?: 0
            }

            if (msg.sent != null && msg.sent == "true") {
                userLabel.username = sendingUser?.profile?.name
            } else {
                if (msg.user != null && msg.user?.isNotEmpty() == true) {
                    userLabel.username = msg.user
                } else {
                    userLabel.username = context.getString(R.string.system)
                }
            }

            messageText.text = chatMessage?.parsedText
            if (msg.parsedText == null) {
                messageText.text = chatMessage?.text
                Maybe.just(chatMessage?.text ?: "")
                        .map { MarkdownParser.parseMarkdown(it) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ parsedText ->
                            chatMessage?.parsedText = parsedText
                            messageText.text = chatMessage?.parsedText
                        }, { it.printStackTrace() })
            }
            this.messageText.movementMethod = LinkMovementMethod.getInstance()

            agoLabel.text = msg.getAgoString(res)

            msg.userStyles.notNull {
                avatarView.setAvatar(it)
            }

            if (expandedMessageId == msg.id) {
                buttonsWrapper.visibility = View.VISIBLE
                deleteButton.visibility = if (shouldShowDelete()) View.VISIBLE else View.GONE
                replyButton.visibility = if (chatMessage?.isInboxMessage == true) View.GONE else View.VISIBLE
            } else {
                buttonsWrapper.visibility = View.GONE
            }
        }

        private fun setLikeProperties() {
            likeBackground.visibility = if (isTavern) View.VISIBLE else View.INVISIBLE
            tvLikes.text = "+" + chatMessage?.likeCount

            val backgroundColorRes: Int
            val foregroundColorRes: Int

            if (chatMessage?.likeCount != 0) {
                if (chatMessage?.userLikesMessage(userId) == true) {
                    backgroundColorRes = R.color.tavern_userliked_background
                    foregroundColorRes = R.color.tavern_userliked_foreground
                } else {
                    backgroundColorRes = R.color.tavern_somelikes_background
                    foregroundColorRes = R.color.tavern_somelikes_foreground
                }
            } else {
                backgroundColorRes = R.color.tavern_nolikes_background
                foregroundColorRes = R.color.tavern_nolikes_foreground
            }

            DataBindingUtils.setRoundedBackground(likeBackground, ContextCompat.getColor(context, backgroundColorRes))
            tvLikes.setTextColor(ContextCompat.getColor(context, foregroundColorRes))
        }

        private fun shouldShowDelete(): Boolean {
            return chatMessage?.isSystemMessage != true && (chatMessage?.uuid == userId || user?.contributor != null && user.contributor.admin)
        }
    }
}
