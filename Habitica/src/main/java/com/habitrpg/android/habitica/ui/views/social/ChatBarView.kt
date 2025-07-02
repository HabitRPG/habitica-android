package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ChatBarViewBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.helpers.AutocompleteAdapter
import com.habitrpg.android.habitica.ui.helpers.AutocompleteTokenizer
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.OnImeVisibilityChangedListener
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.MainNavigationController

class ChatBarView : LinearLayout, OnImeVisibilityChangedListener {
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

    private var safeInsets: Insets = Insets.NONE
    private var imeHeight: Int = 0

    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView()
    }

    var autocompleteAdapter: AutocompleteAdapter? = null

    private fun setupView() {
        orientation = VERTICAL
        this.setBackgroundResource(R.color.content_background)

        binding.chatEditText.addTextChangedListener(
            OnChangeTextWatcher { _, _, _, _ ->
                setSendButtonEnabled(binding.chatEditText.text.isNotEmpty() && binding.chatEditText.text.length <= maxChatLength)
                updateTextIndicator(binding.chatEditText.text.toString())
            }
        )
        binding.chatEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendButtonPressed()
                if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val inputService = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputService.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
            true
        }

        binding.sendButton.setOnClickListener { sendButtonPressed() }

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

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)

        if (visibility == VISIBLE) {
            KeyboardUtil.addImeVisibilityListener(this)
        } else {
            KeyboardUtil.removeImeVisibilityListener(this)
        }
    }

    private fun updateTextIndicator(text: String) {
        if (binding.chatEditText.lineCount >= 3) {
            binding.textIndicator.visibility = View.VISIBLE
            binding.indicatorSpacing.visibility = View.VISIBLE
            binding.textIndicator.text = "${text.length}/$maxChatLength"
            val color =
                when {
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
        val tintColor: Int =
            if (enabled) {
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

    override fun onImeVisibilityChanged(visible: Boolean, height: Int, safeInsets: Insets) {
        val navInset = safeInsets.bottom
        val imeOffset = if (visible) (height - navInset).coerceAtLeast(0) else 0

        updatePadding(
            left   = safeInsets.left,
            right  = safeInsets.right,
            bottom = navInset
        )
        
        translationY = -imeOffset.toFloat()
    }

}
