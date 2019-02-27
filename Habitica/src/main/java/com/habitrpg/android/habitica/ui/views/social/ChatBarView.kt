package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.ui.helpers.AutocompleteAdapter
import com.habitrpg.android.habitica.ui.helpers.ChatInputTokenizer
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import net.pherth.android.emoji_library.EmojiPopup
import javax.inject.Inject


class ChatBarView : FrameLayout {

    @Inject
    lateinit var socialRepository: SocialRepository

    private val sendButton: ImageButton by bindView(R.id.sendButton)
    private val chatEditText: MultiAutoCompleteTextView by bindView(R.id.chatEditText)
    private val textIndicator: TextView by bindView(R.id.text_indicator)
    private val indicatorSpacing: View by bindView(R.id.indicator_spacing)
    private val emojiButton: ImageButton by bindView(R.id.emojiButton)
    private val spacing: Space by bindView(R.id.spacing)
    private val popup: EmojiPopup by lazy {
        EmojiPopup(emojiButton.rootView, context, ContextCompat.getColor(context, R.color.brand))
    }
    private var navBarAccountedHeightCalculated = false

    internal var maxChatLength = 3000

    var sendAction: ((String) -> Unit)? = null

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        inflater?.inflate(R.layout.tavern_chat_new_entry_item, this)
        this.setBackgroundResource(R.color.white)

        HabiticaBaseApplication.component?.inject(this)

        chatEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setSendButtonEnabled(chatEditText.text.isNotEmpty() && chatEditText.text.length <= maxChatLength)
                updateTextIndicator(chatEditText.text.toString())
            }
        })

        sendButton.setOnClickListener { sendButtonPressed() }

        emojiButton.setOnClickListener(EmojiClickListener(chatEditText))

        popup.setSizeForSoftKeyboard()
        popup.setOnDismissListener { changeEmojiKeyboardIcon(false) }
        popup.setOnSoftKeyboardOpenCloseListener(object : EmojiPopup.OnSoftKeyboardOpenCloseListener {

            override fun onKeyboardOpen(keyBoardHeight: Int) {

            }

            override fun onKeyboardClose() {
                if (popup.isShowing) {
                    popup.dismiss()
                }
            }
        })

        popup.setOnEmojiconClickedListener { emojicon ->
            val start = chatEditText.selectionStart
            val end = chatEditText.selectionEnd
            if (start < 0) {
                chatEditText.append(emojicon.emoji)
            } else {
                chatEditText.text?.replace(Math.min(start, end),
                        Math.max(start, end), emojicon.emoji, 0,
                        emojicon.emoji.length)
            }
        }

        popup.setOnEmojiconBackspaceClickedListener {
            val event = KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            chatEditText.dispatchKeyEvent(event)
        }
        resizeForDrawingUnderNavbar()

        val tagArray = AutocompleteAdapter(context, socialRepository)
        chatEditText.setAdapter(tagArray)
        chatEditText.threshold = 2

        chatEditText.setTokenizer(ChatInputTokenizer())
    }

    private fun updateTextIndicator(text: String) {
        if (chatEditText.lineCount >= 3) {
            textIndicator.visibility = View.VISIBLE
            indicatorSpacing.visibility = View.VISIBLE
            textIndicator.text = "${text.length}/$maxChatLength"
            val color = when {
                text.length > maxChatLength -> R.color.red_50
                text.length > (maxChatLength * 0.95) -> R.color.yellow_5
                else -> R.color.gray_400
            }
            textIndicator.setTextColor(ContextCompat.getColor(context, color))
        } else {
            textIndicator.visibility = View.GONE
            indicatorSpacing.visibility = View.GONE
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            resizeForDrawingUnderNavbar()
        }
    }

    //https://github.com/roughike/BottomBar/blob/master/bottom-bar/src/main/java/com/roughike/bottombar/BottomBar.java#L834
    private fun resizeForDrawingUnderNavbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val currentHeight = height

            if (currentHeight != 0 && !navBarAccountedHeightCalculated) {
                navBarAccountedHeightCalculated = true

                val navbarHeight = NavbarUtils.getNavbarHeight(context)
                spacing.updateLayoutParams<LinearLayout.LayoutParams> {
                    height = navbarHeight
                }
            }
        }
    }

    private fun setSendButtonEnabled(enabled: Boolean) {
        val tintColor: Int = if (enabled) {
            ContextCompat.getColor(context, R.color.brand_400)
        } else {
            ContextCompat.getColor(context, R.color.gray_400)
        }
        sendButton.setColorFilter(tintColor)
        sendButton.isEnabled = enabled
    }

    private fun sendButtonPressed() {
        val chatText = chatEditText.text.toString()
        if (chatText.isNotEmpty()) {
            chatEditText.text = null
            sendAction?.invoke(chatText)
        }
    }

    private fun changeEmojiKeyboardIcon(keyboardOpened: Boolean) {
        if (keyboardOpened) {
            emojiButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_grey600_24dp))
        } else {
            emojiButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_emoticon_grey600_24dp))
        }
    }

    private inner class EmojiClickListener internal constructor(internal var view: MultiAutoCompleteTextView) : View.OnClickListener {

        override fun onClick(v: View) {
            if (!popup.isShowing) {
                if (popup.isKeyBoardOpen == true) {
                    popup.showAtBottom()
                    changeEmojiKeyboardIcon(true)
                } else {
                    view.isFocusableInTouchMode = true
                    view.requestFocus()
                    popup.showAtBottomPending()
                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                    changeEmojiKeyboardIcon(true)
                }
            } else {
                popup.dismiss()
                changeEmojiKeyboardIcon(false)
            }
        }
    }
}
