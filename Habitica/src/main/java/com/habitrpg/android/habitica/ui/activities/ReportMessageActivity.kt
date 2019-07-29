package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import io.reactivex.functions.Consumer
import javax.inject.Inject


class ReportMessageActivity : BaseActivity() {

    @Inject
    lateinit var socialRepository: SocialRepository

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val toolbarTextView: TextView by bindView(R.id.toolbar_title)
    private val closeButton: ImageButton by bindView(R.id.close_button)
    private val reportButton: Button by bindView(R.id.report_button)
    private val appBar: AppBarLayout by bindView(R.id.app_bar)
    private val bottomSheetView: View by bindView(R.id.bottom_sheet)
    private val contentContainer: ViewGroup by bindView(R.id.content_container)
    private val dismissTouchView: View by bindView(R.id.touch_outside)
    private val titleTextView: TextView by bindView(R.id.title_text_view)
    private val messageTextView: TextView by bindView(R.id.message_text_view)
    private val additionInfoEditText: EditText by bindView(R.id.additional_info_edittext)
    private val reportExplanationTextView: TextView by bindView(R.id.report_explanation_textview)
    private var raisedElevation = 0f

    private var messageID: String? = null
    private var isReporting: Boolean = false

    override fun getLayoutResId(): Int {
        return R.layout.activity_report_message
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private var chatMessage: ChatMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        raisedElevation = appBar.elevation
        setStatusBarDim(true)

        contentContainer.setOnTouchListener { _, _ -> true }
        dismissTouchView.setOnClickListener { finish() }
        reportExplanationTextView.text = MarkdownParser.parseMarkdown(getString(R.string.report_explanation))

        BottomSheetBehavior.from<View>(bottomSheetView)
                .setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    @SuppressLint("SwitchIntDef")
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN -> finish()
                            BottomSheetBehavior.STATE_EXPANDED -> setStatusBarDim(false)
                            else -> setStatusBarDim(true)
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // no op
                    }
                })

        val args = navArgs<ReportMessageActivityArgs>().value
        messageID = args.messageID
        titleTextView.text = getString(R.string.report_message_title, args.profileName)
        messageTextView.text = args.text

        messageID?.let {messageID ->
            compositeSubscription.add(socialRepository.getChatmessage(messageID).subscribe(Consumer {
                chatMessage = it
            }, RxErrorHandler.handleEmptyError()))
        }

        reportButton.setOnClickListener { reportMessage() }
        closeButton.setOnClickListener { finish() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun reportMessage() {
        if (isReporting) {
            return
        }
        chatMessage?.let {
            isReporting = true
            socialRepository.flagMessage(it, additionInfoEditText.text.toString())
                    .doOnError { isReporting = false }
                    .subscribe(Consumer {
                finish()
            }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun setStatusBarDim(dim: Boolean) {
        if (dim) {
            appBar.elevation = 0f
            window.statusBarColor = ContextCompat.getColor(this, R.color.brand_50)
            closeButton.visibility = View.GONE
            toolbarTextView.setTypeface(null, Typeface.BOLD)
        } else {
            appBar.elevation = 8f
            window.statusBarColor = ContextCompat.getColor(this, R.color.gray_600)
            closeButton.visibility = View.VISIBLE
            toolbarTextView.setTypeface(null, Typeface.NORMAL)
        }

        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            setSystemBarTheme(dim)
        }
    }

    override fun finish() {
        dismissKeyboard()
        super.finish()
    }

    @RequiresApi(api = VERSION_CODES.M)
    fun setSystemBarTheme(isDark: Boolean) {
        // Fetch the current flags.
        val lFlags = window.decorView.systemUiVisibility
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        window.decorView.systemUiVisibility = if (isDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}
