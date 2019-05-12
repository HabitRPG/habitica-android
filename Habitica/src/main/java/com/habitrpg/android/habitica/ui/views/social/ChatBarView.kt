package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.helpers.AutocompleteAdapter
import com.habitrpg.android.habitica.ui.helpers.AutocompleteTokenizer
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import javax.inject.Inject


class ChatBarView : FrameLayout {

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    private val sendButton: ImageButton by bindView(R.id.sendButton)
    private val chatEditText: MultiAutoCompleteTextView by bindView(R.id.chatEditText)
    private val textIndicator: TextView by bindView(R.id.text_indicator)
    private val indicatorSpacing: View by bindView(R.id.indicator_spacing)
    private val spacing: Space by bindView(R.id.spacing)
    private var navBarAccountedHeightCalculated = false

    var chatMessages: List<ChatMessage>
        get() = autocompleteAdapter?.chatMessages ?: listOf()
        set(value) {
            autocompleteAdapter?.chatMessages = value
        }

    internal var maxChatLength = 3000L

    var sendAction: ((String) -> Unit)? = null
    var autocompleteContext: String = ""
        set(value) {
            field = value
            autocompleteAdapter?.autocompleteContext = value
        }
    var groupID: String? = null
        set(value) {
            field = value
            autocompleteAdapter?.groupID = value
        }

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private var autocompleteAdapter: AutocompleteAdapter? = null

    private fun setupView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        inflater?.inflate(R.layout.tavern_chat_new_entry_item, this)
        this.setBackgroundResource(R.color.white)

        HabiticaBaseApplication.component?.inject(this)

        chatEditText.addTextChangedListener(OnChangeTextWatcher { s, _, _, _ ->
                setSendButtonEnabled(chatEditText.text.isNotEmpty() && chatEditText.text.length <= maxChatLength)
                updateTextIndicator(chatEditText.text.toString())
        })

        sendButton.setOnClickListener { sendButtonPressed() }

        resizeForDrawingUnderNavbar()

        autocompleteAdapter = AutocompleteAdapter(context, socialRepository, autocompleteContext, groupID, appConfigManager.enableUsernameAutocomplete())
        chatEditText.setAdapter(autocompleteAdapter)
        chatEditText.threshold = 2

        chatEditText.setTokenizer(AutocompleteTokenizer(listOf('@', ':')))
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
}
