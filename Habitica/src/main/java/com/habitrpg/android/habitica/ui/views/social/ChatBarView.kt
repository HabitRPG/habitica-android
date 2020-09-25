package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ChatBarViewBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.helpers.AutocompleteAdapter
import com.habitrpg.android.habitica.ui.helpers.AutocompleteTokenizer
import javax.inject.Inject


class ChatBarView : LinearLayout {

    var hasAcceptedGuidelines: Boolean = false
    set(value) {
        field = value
        if (field) {
            binding.communityGuidelinesView.visibility = View.GONE
            binding.chatBarContent.visibility = View.VISIBLE
        } else {
            binding.chatBarContent.visibility = View.GONE
        }
    }
    var onCommunityGuidelinesAccepted: (() -> Unit)? = null

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    private val binding = ChatBarViewBinding.inflate(context.layoutInflater, this)

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

    var message: String
    get() = binding.chatEditText.text.toString()
    set(value) = binding.chatEditText.setText(value, TextView.BufferType.EDITABLE)

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private var autocompleteAdapter: AutocompleteAdapter? = null

    private fun setupView(context: Context) {
        orientation = VERTICAL
        this.setBackgroundResource(R.color.content_background)

        HabiticaBaseApplication.userComponent?.inject(this)

        binding.chatEditText.addTextChangedListener(OnChangeTextWatcher { _, _, _, _ ->
                setSendButtonEnabled(binding.chatEditText.text.isNotEmpty() && binding.chatEditText.text.length <= maxChatLength)
                updateTextIndicator(binding.chatEditText.text.toString())
        })

        binding.sendButton.setOnClickListener { sendButtonPressed() }

        autocompleteAdapter = AutocompleteAdapter(context, socialRepository, autocompleteContext, groupID, appConfigManager.enableUsernameAutocomplete())
        binding.chatEditText.setAdapter(autocompleteAdapter)
        binding.chatEditText.threshold = 2

        binding.chatEditText.setTokenizer(AutocompleteTokenizer(listOf('@', ':')))

        binding.communityGuidelinesAcceptButton.setOnClickListener {
            onCommunityGuidelinesAccepted?.invoke()
        }
        binding.communityGuidelinesReviewView.setOnClickListener {
            MainNavigationController.navigate(R.id.guidelinesActivity)
        }
    }

    private fun updateTextIndicator(text: String) {
        if (binding.chatEditText.lineCount >= 3) {
            binding.textIndicator.visibility = View.VISIBLE
            binding.indicatorSpacing.visibility = View.VISIBLE
            binding.textIndicator.text = "${text.length}/$maxChatLength"
            val color = when {
                text.length > maxChatLength -> R.color.text_red
                text.length > (maxChatLength * 0.95) -> R.color.text_yellow
                else -> R.color.text_dimmed
            }
            binding.textIndicator.setTextColor(ContextCompat.getColor(context, color))
        } else {
            binding.textIndicator.visibility = View.GONE
            binding.indicatorSpacing.visibility = View.GONE
        }
    }

    private fun setSendButtonEnabled(enabled: Boolean) {
        val tintColor: Int = if (enabled) {
            context.getThemeColor(R.attr.colorAccent)
        } else {
            ContextCompat.getColor(context, R.color.disabled_background)
        }
        binding.sendButton.setColorFilter(tintColor)
        binding.sendButton.isEnabled = enabled
    }

    private fun sendButtonPressed() {
        val chatText = message
        if (chatText.isNotEmpty()) {
            binding.chatEditText.text = null
            sendAction?.invoke(chatText)
        }
    }
}
