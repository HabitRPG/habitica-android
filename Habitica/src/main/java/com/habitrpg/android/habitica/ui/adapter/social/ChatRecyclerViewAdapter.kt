package com.habitrpg.android.habitica.ui.adapter.social

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
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

    private val likeMessageEvents = PublishSubject.create<ChatMessage>()
    private val userLabelClickEvents = PublishSubject.create<String>()
    private val privateMessageClickEvents = PublishSubject.create<String>()
    private val deleteMessageEvents = PublishSubject.create<ChatMessage>()
    private val flagMessageEvents = PublishSubject.create<ChatMessage>()
    private val copyMessageAsTodoEvents = PublishSubject.create<ChatMessage>()
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

    fun getPrivateMessageClickFlowable(): Flowable<String> {
        return privateMessageClickEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getFlagMessageClickFlowable(): Flowable<ChatMessage> {
        return flagMessageEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getDeleteMessageFlowable(): Flowable<ChatMessage> {
        return deleteMessageEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getCopyMessageAsTodoFlowable(): Flowable<ChatMessage> {
        return copyMessageAsTodoEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getCopyMessageFlowable(): Flowable<ChatMessage> {
        return copyMessageEvents.toFlowable(BackpressureStrategy.DROP)
    }


    inner class ChatRecyclerViewHolder(itemView: View, private val userId: String, private val isTavern: Boolean) : RecyclerView.ViewHolder(itemView), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private val btnOptions: ImageView by bindView(R.id.btn_options)
        private val userBackground: LinearLayout by bindView(R.id.user_background_layout)
        private val userLabel: TextView by bindView(R.id.user_label)
        private val messageText: EmojiTextView by bindView(R.id.message_text)
        private val agoLabel: TextView by bindView(R.id.ago_label)
        private val likeBackground: LinearLayout by bindView(R.id.like_background_layout)
        private val tvLikes: TextView by bindView(R.id.tvLikes)

        val context: Context = itemView.context
        val res: Resources = itemView.resources
        private var chatMessage: ChatMessage? = null

        init {
            btnOptions.setOnClickListener(this)
            tvLikes.setOnClickListener { toggleLike() }
        }

        fun bind(msg: ChatMessage) {
            chatMessage = msg

            setLikeProperties()

            if (msg.sent != null && msg.sent == "true" && sendingUser != null) {
                DataBindingUtils.setRoundedBackgroundInt(userBackground, sendingUser!!.contributorColor)
            } else {
                DataBindingUtils.setRoundedBackgroundInt(userBackground, msg.contributorColor)
            }

            if (msg.sent != null && msg.sent == "true") {
                userLabel.text = sendingUser?.profile?.name
            } else {
                if (msg.user != null && msg.user?.isNotEmpty() == true) {
                    userLabel.text = msg.user
                } else {
                    userLabel.setText(R.string.system)
                }
            }

            userLabel.isClickable = true
            userLabel.setOnClickListener { view -> userLabelClickEvents.onNext(msg.uuid ?: "") }

            DataBindingUtils.setForegroundTintColor(userLabel, msg.contributorForegroundColor)

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

        override fun onClick(v: View) {
            if (chatMessage != null) {
                if (btnOptions === v) {
                    val popupMenu = PopupMenu(context, v)

                    //set my own listener giving the View that activates the event onClick (i.e. YOUR ImageView)
                    popupMenu.setOnMenuItemClickListener(this)
                    //inflate your PopUpMenu
                    popupMenu.menuInflater.inflate(R.menu.chat_message, popupMenu.menu)

                    // Force icons to show
                    var menuHelper: Any? = null
                    var argTypes: Array<Class<*>>
                    try {
                        val fMenuHelper = PopupMenu::class.java.getDeclaredField("mPopup")
                        fMenuHelper.isAccessible = true
                        menuHelper = fMenuHelper.get(popupMenu)
                        argTypes = arrayOf(Boolean::class.java)
                        menuHelper?.javaClass?.getDeclaredMethod("setForceShowIcon", *argTypes)?.invoke(menuHelper, true)
                    } catch (ignored: Exception) {
                    }


                    popupMenu.menu.findItem(R.id.menu_chat_delete).isVisible = shouldShowDelete(chatMessage)
                    popupMenu.menu.findItem(R.id.menu_chat_flag).isVisible = chatMessage?.uuid != "system"
                    popupMenu.menu.findItem(R.id.menu_chat_copy_as_todo).isVisible = false
                    popupMenu.menu.findItem(R.id.menu_chat_send_pm).isVisible = false

                    popupMenu.show()

                    // Try to force some horizontal offset
                    try {
                        val fListPopup = menuHelper?.javaClass?.getDeclaredField("mPopup")
                        fListPopup?.isAccessible = true
                        val listPopup = fListPopup?.get(menuHelper)
                        argTypes = arrayOf(Int::class.java)
                        val listPopupClass = listPopup?.javaClass

                        // Get the width of the popup window
                        val width = listPopupClass?.getDeclaredMethod("getWidth")?.invoke(listPopup) as Int?

                        // Invoke setHorizontalOffset() with the negative width to move left by that distance
                        listPopupClass?.getDeclaredMethod("setHorizontalOffset", *argTypes)?.invoke(listPopup, -(width ?: 0))

                        // Invoke show() to update the window's position
                        listPopupClass?.getDeclaredMethod("show")?.invoke(listPopup)
                    } catch (ignored: Exception) {

                    }

                }
            }
        }

        private fun shouldShowDelete(chatMsg: ChatMessage?): Boolean {
            return chatMsg?.isSystemMessage != true && (chatMsg?.uuid == userId || user?.contributor != null && user.contributor.admin)
        }

        fun toggleLike() {
            chatMessage.notNull { likeMessageEvents.onNext(it) }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            chatMessage.notNull {
                when (item.itemId) {
                    R.id.menu_chat_delete -> {
                        deleteMessageEvents.onNext(it)
                    }
                    R.id.menu_chat_flag -> {
                        flagMessageEvents.onNext(it)
                    }
                    R.id.menu_chat_copy_as_todo -> {
                        copyMessageAsTodoEvents.onNext(it)
                    }

                    R.id.menu_chat_send_pm -> {
                        privateMessageClickEvents.onNext(it.uuid ?: "")
                    }

                    R.id.menu_chat_copy -> {
                        copyMessageEvents.onNext(it)
                    }
                }
            }
            return false
        }
    }
}
