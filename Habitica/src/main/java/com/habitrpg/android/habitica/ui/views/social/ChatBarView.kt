package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils

class ChatBarView : FrameLayout {

    private val chatBarContainer: LinearLayout by bindView(R.id.chatBarContainer)
    private val sendButton: ImageButton by bindView(R.id.sendButton)
    private val chatEditText: AppCompatEditText by bindView(R.id.chatEditText)

    private var navBarAccountedHeightCalculated = false

    var sendAction: ((String) -> Unit)? = null

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.tavern_chat_new_entry_item, this)
        this.setBackgroundResource(R.color.white)

        chatEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setSendButtonEnabled(chatEditText.text.isNotEmpty())
            }
        })

        sendButton.setOnClickListener { sendButtonPressed() }

        resizeForDrawingUnderNavbar()
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
                val layoutParams = this.layoutParams as? LinearLayout.LayoutParams
                layoutParams?.setMargins(0, 0, 0, navbarHeight)
                setLayoutParams(layoutParams)
                (parent as? View)?.invalidate()
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
