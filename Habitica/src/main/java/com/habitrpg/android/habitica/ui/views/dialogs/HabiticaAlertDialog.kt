package com.habitrpg.android.habitica.ui.views.dialogs

import android.app.Activity
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.views.login.LockableScrollView
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.plattysoft.leonids.ParticleSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

open class HabiticaAlertDialog(context: Context) : AlertDialog(context, R.style.HabiticaAlertDialogTheme) {

    var buttonAxis: Int = LinearLayout.VERTICAL
        set(value) {
            field = value
            updateButtonLayout()
        }
    var isCelebratory: Boolean = false
    private val view: RelativeLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as RelativeLayout
    private val dialogWrapper: LinearLayout
    internal val dialogContainer: LinearLayout
    private var titleTextView: TextView
    private var messageTextView: TextView
    internal var contentView: ViewGroup
    private var scrollingSeparator: View
    internal var scrollView: LockableScrollView
    protected var buttonsWrapper: LinearLayout
    private var noticeTextView: TextView
    private var closeButton: Button

    internal var additionalContentView: View? = null

    var isScrollingLayout: Boolean = false
        get() {
            if (forceScrollableLayout) return true
            return field
        }
        set(value) {
            field = value
            updateButtonLayout()
        }
    var forceScrollableLayout = false
        set(value) {
            field = value
            updateButtonLayout()
        }

    var dialogWidth = 320
        set(value) {
            field = value
            val layoutParams = dialogWrapper.layoutParams
            layoutParams.width = value
            dialogWrapper.layoutParams = layoutParams
        }

    init {
        setView(view)
        dialogWrapper = view.findViewById(R.id.dialog_wrapper)
        dialogContainer = view.findViewById(R.id.dialog_container)
        titleTextView = view.findViewById(R.id.titleTextView)
        messageTextView = view.findViewById(R.id.messageTextView)
        contentView = view.findViewById(R.id.content_view)
        scrollingSeparator = view.findViewById(R.id.scrolling_separator)
        scrollView = view.findViewById(R.id.main_scroll_view)
        buttonsWrapper = view.findViewById(R.id.buttons_wrapper)
        noticeTextView = view.findViewById(R.id.notice_text_view)
        closeButton = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener { dismiss() }
        dialogContainer.clipChildren = true
        dialogContainer.clipToOutline = true
    }

    override fun setTitle(title: CharSequence?) {
        if ((title?.length ?: 0) > 0) {
            titleTextView.visibility = View.VISIBLE
        } else {
            titleTextView.visibility = View.GONE
        }
        titleTextView.text = title
    }

    override fun setTitle(titleId: Int) {
        setTitle(context.getString(titleId))
    }

    override fun setMessage(message: CharSequence?) {
        if ((message?.length ?: 0) > 0) {
            messageTextView.visibility = View.VISIBLE
        } else {
            messageTextView.visibility = View.GONE
        }
        messageTextView.text = message
        messageTextView.movementMethod = ScrollingMovementMethod()
    }

    fun setMessage(messageId: Int) {
        setMessage(context.getString(messageId))
    }

    fun setNotice(notice: CharSequence?) {
        if ((notice?.length ?: 0) > 0) {
            noticeTextView.visibility = View.VISIBLE
        } else {
            noticeTextView.visibility = View.GONE
        }
        noticeTextView.text = notice
    }

    fun setNotice(noticeID: Int) {
        setNotice(context.getString(noticeID))
    }

    fun setCustomHeaderView(customHeader: View) {
        dialogContainer.addView(customHeader, 0)
        dialogContainer.setPadding(0, 0, 0, dialogContainer.paddingBottom)
    }

    fun setAdditionalContentView(layoutResID: Int) {
        val inflater = context.layoutInflater
        setAdditionalContentView(inflater.inflate(layoutResID, view, false))
    }

    fun setAdditionalContentView(view: View?) {
        this.view.removeView(additionalContentView)
        additionalContentView = view
        this.contentView.addView(view)
        val layoutParams = view?.layoutParams
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        view?.layoutParams = layoutParams
        contentView.forceLayout()
    }

    fun setAdditionalContentSidePadding(padding: Int) {
        contentView.setPadding(padding, 0, padding, contentView.paddingBottom)
        messageTextView.setPadding(padding, messageTextView.paddingTop, padding, messageTextView.paddingBottom)
    }

    fun setExtraCloseButtonVisibility(visibility: Int) {
        closeButton.visibility = visibility
    }

    private fun updateButtonLayout() {
        if (isScrollingLayout || buttonAxis == LinearLayout.HORIZONTAL) {
            scrollingSeparator.visibility = View.VISIBLE
            buttonsWrapper.orientation = LinearLayout.HORIZONTAL
            val padding = 16.dpToPx(context)
            buttonsWrapper.setPadding(padding, padding, padding, 0)
            dialogContainer.setPadding(0, dialogContainer.paddingTop, 0, padding)
            contentView.setPadding(contentView.paddingStart, 0, contentView.paddingEnd, 30.dpToPx(context))
        } else {
            scrollingSeparator.visibility = View.GONE
            buttonsWrapper.orientation = LinearLayout.VERTICAL
            contentView.setPadding(contentView.paddingStart, 0, contentView.paddingEnd, 0)
            val sidePadding = context.resources.getDimension(R.dimen.alert_side_padding).toInt()
            buttonsWrapper.setPadding(20.dpToPx(context), sidePadding, sidePadding, 0)
            dialogContainer.setPadding(0, dialogContainer.paddingTop, 0, 24.dpToPx(context))
        }
        buttonsWrapper.children.forEach { configureButtonLayoutParams(it) }
    }

    fun getContentView(): View? = additionalContentView

    fun addButton(
        stringRes: Int,
        isPrimary: Boolean,
        isDestructive: Boolean = false,
        autoDismiss: Boolean = true,
        function: ((HabiticaAlertDialog, Int) -> Unit)? = null
    ): Button {
        return addButton(context.getString(stringRes), isPrimary, isDestructive, autoDismiss, function)
    }

    fun addButton(
        string: String,
        isPrimary: Boolean,
        isDestructive: Boolean = false,
        autoDismiss: Boolean = true,
        function: ((HabiticaAlertDialog, Int) -> Unit)? = null
    ): Button {
        val button: Button = if (isPrimary) {
            if (isDestructive) {
                buttonsWrapper.inflate(R.layout.dialog_habitica_primary_destructive_button) as? Button
            } else {
                buttonsWrapper.inflate(R.layout.dialog_habitica_primary_button) as? Button
            }
        } else {
            val button = buttonsWrapper.inflate(R.layout.dialog_habitica_secondary_button) as? Button
            if (isDestructive) {
                button?.setTextColor(ContextCompat.getColor(context, R.color.text_red))
            }
            button
        } ?: Button(context)
        button.text = string
        return addButton(button, autoDismiss, function) as Button
    }

    fun addButton(
        buttonView: View,
        autoDismiss: Boolean = true,
        function: ((HabiticaAlertDialog, Int) -> Unit)? = null
    ): View {
        val weakThis = WeakReference(this)
        val buttonIndex = buttonsWrapper.childCount
        buttonView.setOnClickListener {
            weakThis.get()?.let { it1 ->
                if (function != null) {
                    function(it1, buttonIndex)
                }
                if (autoDismiss) {
                    dismiss()
                }
            }
        }
        configureButtonLayoutParams(buttonView)
        buttonsWrapper.addView(buttonView)
        // for some reason the padding gets lost somewhere.
        buttonView.setPadding(24.dpToPx(context), 0, 24.dpToPx(context), 0)
        return buttonView
    }

    private fun configureButtonLayoutParams(buttonView: View) {
        val layoutParams = if (isScrollingLayout) {
            val params = LinearLayout.LayoutParams(0, 48.dpToPx(context))
            params.weight = 1f
            params
        } else {
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 48.dpToPx(context))
        }
        buttonView.layoutParams = layoutParams
        buttonView.elevation = 10f

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    fun enqueue() {
        addToQueue(this)
    }

    override fun dismiss() {
        showNextInQueue(this)
        super.dismiss()
    }

    fun getActivity(): Activity? {
        var thisContext = context
        while (thisContext as? ContextThemeWrapper != null && thisContext as? Activity == null) {
            thisContext = thisContext.baseContext
        }
        return thisContext as? Activity
    }

    override fun onStart() {
        super.onStart()

        if (isCelebratory) {
            titleTextView.post {
                val confettiContainer = view.findViewById<RelativeLayout>(R.id.confetti_container)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_blue), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(titleTextView, Gravity.BOTTOM, 10, 2000)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_red), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(titleTextView, Gravity.BOTTOM, 10, 2000)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_yellow), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(titleTextView, Gravity.BOTTOM, 10, 2000)
                ParticleSystem(confettiContainer, 40, ContextCompat.getDrawable(context, R.drawable.confetti_purple), 6000)
                    .setAcceleration(0.00010f, 90)
                    .setRotationSpeed(144f)
                    .setSpeedByComponentsRange(-0.15f, 0.15f, -0.1f, -0.4f)
                    .setFadeOut(200, AccelerateInterpolator())
                    .emitWithGravity(titleTextView, Gravity.BOTTOM, 10, 2000)
            }
        }
    }

    companion object {
        private var dialogQueue = mutableListOf<HabiticaAlertDialog>()

        private fun showNextInQueue(currentDialog: HabiticaAlertDialog) {
            if (dialogQueue.firstOrNull() == currentDialog) {
                dialogQueue.removeAt(0)
            }
            if (dialogQueue.size > 0) {
                if ((dialogQueue[0].context as? BaseActivity)?.isFinishing != true) {
                    (dialogQueue[0].context as? BaseActivity)?.lifecycleScope?.launch(context = Dispatchers.Main) {
                        delay(500L)
                        if (dialogQueue.size > 0 && (
                            (dialogQueue[0].context as? Activity)?.isFinishing == false ||
                                ((dialogQueue[0].context as? ContextThemeWrapper)?.baseContext as? Activity)?.isFinishing == false
                            )
                        ) {
                            dialogQueue[0].show()
                        }
                    }
                }
            }
        }

        private fun addToQueue(dialog: HabiticaAlertDialog) {
            if (checkIfQueueAvailable()) {
                dialog.show()
            }
            dialogQueue.add(dialog)
        }

        private fun checkIfQueueAvailable(): Boolean {
            val currentDialog = dialogQueue.firstOrNull() ?: return true
            return if (currentDialog.isShowing && currentDialog.getActivity()?.isFinishing != true) {
                false
            } else {
                // The Dialog was probably dismissed in a weird way. Clear it out so that the queue doesn't get stuck
                dialogQueue.removeAt(0)
                true
            }
        }
    }
}
