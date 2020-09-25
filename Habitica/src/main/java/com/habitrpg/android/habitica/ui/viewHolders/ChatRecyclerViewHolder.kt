package com.habitrpg.android.habitica.ui.viewHolders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ChatItemBinding
import com.habitrpg.android.habitica.databinding.TavernChatIntroItemBinding
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.getAgoString
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.habitrpg.android.habitica.models.members.Member

open class ChatRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ChatRecyclerIntroViewHolder(itemView: View, replyToUUID: String) : ChatRecyclerViewHolder(itemView) {
    private val binding = TavernChatIntroItemBinding.bind(itemView)

    var onOpenProfile: ((String) -> Unit)? = null

    init {
        binding.avatarView.setOnClickListener { onOpenProfile?.invoke(replyToUUID) }
        binding.displayNameTextview.setOnClickListener { onOpenProfile?.invoke(replyToUUID) }
        binding.sublineTextview.setOnClickListener { onOpenProfile?.invoke(replyToUUID) }
    }

    fun bind(member: Member) {
        binding.avatarView.setAvatar(member)
        binding.displayNameTextview.username = member.displayName
        binding.displayNameTextview.tier = member.contributor?.level ?: 0
        binding.sublineTextview.text = member.formattedUsername
    }
}


class ChatRecyclerMessageViewHolder(itemView: View, private var userId: String, private val isTavern: Boolean) : ChatRecyclerViewHolder(itemView) {
    val binding = ChatItemBinding.bind(itemView)

    val context: Context = itemView.context
    val res: Resources = itemView.resources
    private var chatMessage: ChatMessage? = null
    private var user: User? = null

    var onShouldExpand: (() -> Unit)? = null
    var onLikeMessage: ((ChatMessage) -> Unit)? = null
    var onOpenProfile: ((String) -> Unit)? = null
    var onReply: ((String) -> Unit)? = null
    var onCopyMessage: ((ChatMessage) -> Unit)? = null
    var onFlagMessage: ((ChatMessage) -> Unit)? = null
    var onDeleteMessage: ((ChatMessage) -> Unit)? = null

    init {
        itemView.setOnClickListener {
            onShouldExpand?.invoke()
        }
        binding.tvLikes.setOnClickListener { chatMessage?.let { onLikeMessage?.invoke(it) } }
        binding.messageText.setOnClickListener { onShouldExpand?.invoke() }
        binding.messageText.movementMethod = LinkMovementMethod.getInstance()
        binding.userLabel.setOnClickListener { chatMessage?.uuid?.let { onOpenProfile?.invoke(it) } }
        binding.avatarView.setOnClickListener { chatMessage?.uuid?.let { onOpenProfile?.invoke(it) } }
        binding.replyButton.setOnClickListener {
            if (chatMessage?.username != null) {
                chatMessage?.username?.let { onReply?.invoke(it) }
            } else {
                chatMessage?.user?.let { onReply?.invoke(it) }
            }
        }
        binding.replyButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatReplyIcon()),
                null, null, null)
        binding.copyButton.setOnClickListener { chatMessage?.let { onCopyMessage?.invoke(it) } }
        binding.copyButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatCopyIcon()),
                null, null, null)
        binding.reportButton.setOnClickListener { chatMessage?.let { onFlagMessage?.invoke(it) } }
        binding.reportButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatReportIcon()),
                null, null, null)
        binding.deleteButton.setOnClickListener { chatMessage?.let { onDeleteMessage?.invoke(it) } }
        binding.deleteButton.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(res, HabiticaIconsHelper.imageOfChatDeleteIcon()),
                null, null, null)
    }

    fun bind(msg: ChatMessage, uuid: String, user: User?, isExpanded: Boolean) {
        chatMessage = msg
        this.user = user
        userId = uuid

        setLikeProperties()

        val wasSent = messageWasSent()

        val name = user?.profile?.name
        if (wasSent) {
            binding.userLabel.isNPC = user?.backer?.npc != null
            binding.userLabel.tier = user?.contributor?.level ?: 0
            binding.userLabel.username = name
            if (user?.username != null) {
                @SuppressLint("SetTextI18n")
                binding.sublineTextview.text = "${user.formattedUsername} ∙ ${msg.timestamp?.getAgoString(res)}"
            } else {
                binding.sublineTextview.text = msg.timestamp?.getAgoString(res)
            }
        } else {
            binding.userLabel.isNPC = msg.backer?.npc != null
            binding.userLabel.tier = msg.contributor?.level ?: 0
            binding.userLabel.username = msg.user
            if (msg.username != null) {
                @SuppressLint("SetTextI18n")
                binding.sublineTextview.text = "${msg.formattedUsername} ∙ ${msg.timestamp?.getAgoString(res)}"
            } else {
                binding.sublineTextview.text = msg.timestamp?.getAgoString(res)
            }
        }
        when (binding.userLabel.tier) {
            8 -> {
                binding.modView.visibility = View.VISIBLE
                binding.modView.text = context.getString(R.string.moderator)
                binding.modView.background = ContextCompat.getDrawable(context, R.drawable.pill_bg_blue)
                binding.modView.setScaledPadding(context, 12, 4, 12, 4)
            }
            9 -> {
                binding.modView.visibility = View.VISIBLE
                binding.modView.text = context.getString(R.string.staff)
                binding.modView.background = ContextCompat.getDrawable(context, R.drawable.pill_bg_purple_300)
                binding.modView.setScaledPadding(context, 12, 4, 12, 4)
            }
            else -> binding.modView.visibility = View.GONE
        }

        if (wasSent) {
            binding.avatarView.visibility = View.GONE
            itemView.setPadding(64.dpToPx(context), itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)
        } else {
            val displayMetrics = res.displayMetrics
            val dpWidth = displayMetrics.widthPixels / displayMetrics.density
            if (dpWidth > 350) {
                binding.avatarView.visibility = View.VISIBLE
                msg.userStyles?.let {
                    binding.avatarView.setAvatar(it)
                }
            } else {
                binding.avatarView.visibility = View.GONE
            }
            itemView.setPadding(16.dpToPx(context), itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)
        }

        binding.messageText.text = chatMessage?.parsedText
        if (msg.parsedText == null) {
            binding.messageText.text = chatMessage?.text
            Maybe.just(chatMessage?.text ?: "")
                    .map { MarkdownParser.parseMarkdown(it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ parsedText ->
                        chatMessage?.parsedText = parsedText
                        binding.messageText.text = chatMessage?.parsedText
                    }, { it.printStackTrace() })
        }

        val username = user?.formattedUsername
        binding.messageWrapper.background = if ((name != null && msg.text?.contains("@$name") == true) || (username != null && msg.text?.contains(username) == true)) {
            ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_brand_700)
        } else {
            ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_window)
        }
        binding.messageWrapper.setScaledPadding(context, 8, 8, 8, 8)

        if (isExpanded) {
            binding.buttonsWrapper.visibility = View.VISIBLE
            binding.deleteButton.visibility = if (shouldShowDelete()) View.VISIBLE else View.GONE
            binding.replyButton.visibility = if (chatMessage?.isInboxMessage == true) View.GONE else View.VISIBLE
        } else {
            binding.buttonsWrapper.visibility = View.GONE
        }
    }

    private fun messageWasSent(): Boolean {
        return chatMessage?.sent == true || chatMessage?.uuid == userId
    }

    private fun setLikeProperties() {
        binding.likeBackgroundLayout.visibility = if (isTavern) View.VISIBLE else View.INVISIBLE
        @SuppressLint("SetTextI18n")
        binding.tvLikes.text = "+" + chatMessage?.likeCount

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

        DataBindingUtils.setRoundedBackground(binding.likeBackgroundLayout, ContextCompat.getColor(context, backgroundColorRes))
        binding.tvLikes.setTextColor(ContextCompat.getColor(context, foregroundColorRes))
    }

    private fun shouldShowDelete(): Boolean {
        return chatMessage?.isSystemMessage != true && (chatMessage?.uuid == userId || user?.contributor?.admin == true || chatMessage?.isInboxMessage == true)
    }
}