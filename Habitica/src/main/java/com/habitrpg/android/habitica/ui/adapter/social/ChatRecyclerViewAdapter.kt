package com.habitrpg.android.habitica.ui.adapter.social

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.*
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
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

class ChatRecyclerViewAdapter(data: OrderedRealmCollection<ChatMessage>?, autoUpdate: Boolean, private val user: User?, private val isTavern: Boolean, private val releasedUsernames: Boolean) : RealmRecyclerViewAdapter<ChatMessage, RecyclerView.ViewHolder>(data, autoUpdate) {
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
            ChatRecyclerViewHolder(parent.inflate(R.layout.tavern_chat_item), uuid, isTavern)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        data.notNull {
            if (it[position].isSystemMessage) {
                (holder as? SystemChatMessageViewHolder)?.bind(it[position])
            } else {
                (holder as? ChatRecyclerViewHolder)?.bind(it[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data?.get(position)?.isSystemMessage == true) 0 else 1
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

    inner class SystemChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView by bindView(R.id.text_view)

        fun bind(chatMessage: ChatMessage?) {
            textView.text = chatMessage?.text?.removePrefix("`")?.removeSuffix("`")
        }

    }

    inner class ChatRecyclerViewHolder(itemView: View, private val userId: String, private val isTavern: Boolean) : RecyclerView.ViewHolder(itemView) {

        private val messageWrapper: ViewGroup by bindView(R.id.message_wrapper)
        private val avatarView: AvatarView by bindView(R.id.avatar_view)
        private val userLabel: UsernameLabel by bindView(R.id.user_label)
        private val messageText: EmojiTextView by bindView(R.id.message_text)
        private val sublineTextView: TextView by bindView(R.id.subline_textview)
        private val likeBackground: LinearLayout by bindView(R.id.like_background_layout)
        private val tvLikes: TextView by bindView(R.id.tvLikes)
        private val buttonsWrapper: ViewGroup by bindView(R.id.buttons_wrapper)
        private val replyButton: Button by bindView(R.id.reply_button)
        private val copyButton: Button by bindView(R.id.copy_button)
        private val reportButton: Button by bindView(R.id.report_button)
        private val deleteButton: Button by bindView(R.id.delete_button)
        private val modView: TextView by bindView(R.id.mod_view)

        val context: Context = itemView.context
        val res: Resources = itemView.resources
        private var chatMessage: ChatMessage? = null

        init {
            itemView.setOnClickListener {
                expandMessage()
            }
            tvLikes.setOnClickListener { _ -> chatMessage.notNull { likeMessageEvents.onNext(it) } }
            messageText.setOnClickListener { _ -> expandMessage() }
            messageText.movementMethod = LinkMovementMethod.getInstance()
            userLabel.setOnClickListener { _ -> chatMessage?.uuid.notNull {userLabelClickEvents.onNext(it) } }
            replyButton.setOnClickListener { _ ->
                if (releasedUsernames && chatMessage?.username != null) {
                    chatMessage?.username.notNull { replyMessageEvents.onNext(it) }
                } else {
                    chatMessage?.user.notNull { replyMessageEvents.onNext(it) }
                }
            }
            replyButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatReplyIcon()),
                    null, null, null)
            copyButton.setOnClickListener { _ -> chatMessage.notNull { copyMessageEvents.onNext(it) } }
            copyButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatCopyIcon()),
                    null, null, null)
            reportButton.setOnClickListener { _ -> chatMessage.notNull { flagMessageEvents.onNext(it) } }
            reportButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatReportIcon()),
                    null, null, null)
            deleteButton.setOnClickListener { _ -> chatMessage.notNull { deleteMessageEvents.onNext(it) } }
            deleteButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatDeleteIcon()),
                    null, null, null)
        }

        fun bind(msg: ChatMessage) {
            chatMessage = msg

            setLikeProperties()

            val name = user?.profile?.name
            if (messageWasSent() && user != null) {
                userLabel.tier = user.contributor?.level ?: 0
                userLabel.username = name
            } else {
                userLabel.tier = msg.contributor?.level ?: 0
                userLabel.username = msg.user
            }
            when {
                userLabel.tier == 8 -> {
                    modView.visibility = View.VISIBLE
                    modView.text = context.getString(R.string.moderator)
                    modView.backgroundCompat = ContextCompat.getDrawable(context, R.drawable.pill_bg_blue)
                    modView.setScaledPadding(context, 12, 4, 12, 4)
                }
                userLabel.tier == 9 -> {
                    modView.visibility = View.VISIBLE
                    modView.text = context.getString(R.string.staff)
                    modView.backgroundCompat = ContextCompat.getDrawable(context, R.drawable.pill_bg_purple_300)
                    modView.setScaledPadding(context, 12, 4, 12, 4)
                }
                else -> modView.visibility = View.GONE
            }

            if (messageWasSent()) {
                avatarView.visibility = View.GONE
                itemView.setPadding(64.dpToPx(context), itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)
            } else {
                val displayMetrics = res.displayMetrics
                val dpWidth = displayMetrics.widthPixels / displayMetrics.density
                if (dpWidth > 350) {
                    avatarView.visibility = View.VISIBLE
                    msg.userStyles.notNull {
                        avatarView.setAvatar(it)
                    }
                } else {
                    avatarView.visibility = View.GONE
                }
                itemView.setPadding(16.dpToPx(context), itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)
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

            val username = user?.username
            if ((name != null && msg.text?.contains(name) == true) || (username != null && msg.text?.contains(username) == true)) {
                messageWrapper.backgroundCompat = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_brand_700)
            } else {
                messageWrapper.backgroundCompat = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg)
            }
            messageWrapper.setScaledPadding(context, 8, 8, 8, 8)

            if (msg.username != null && releasedUsernames) {
                sublineTextView.text = "${msg.formattedUsername} ∙ ${msg.getAgoString(res)}"
            } else {
                sublineTextView.text = msg.getAgoString(res)
            }

            if (expandedMessageId == msg.id) {
                buttonsWrapper.visibility = View.VISIBLE
                deleteButton.visibility = if (shouldShowDelete()) View.VISIBLE else View.GONE
                replyButton.visibility = if (chatMessage?.isInboxMessage == true) View.GONE else View.VISIBLE
                reportButton.visibility = if (chatMessage?.uuid == userId) View.GONE else View.VISIBLE
            } else {
                buttonsWrapper.visibility = View.GONE
            }
        }

        private fun messageWasSent(): Boolean {
            return chatMessage?.sent == true || chatMessage?.uuid == userId
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
            return chatMessage?.isSystemMessage != true && (chatMessage?.uuid == userId || user?.contributor?.admin == true)
        }

        private fun expandMessage() {
            expandedMessageId = if (expandedMessageId == chatMessage?.id) {
                null
            } else {
                chatMessage?.id
            }
            notifyItemChanged(adapterPosition)
        }
    }
}
