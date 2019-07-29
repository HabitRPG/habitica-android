package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setScaledPadding
import java.lang.ref.WeakReference

open class HabiticaAlertDialog(context: Context) : AlertDialog(context, R.style.HabiticaAlertDialogTheme) {

    private val view: LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as LinearLayout
    private val dialogContainer: LinearLayout
    private var titleTextView: TextView
    private var messageTextView: TextView
    internal var contentView: FrameLayout
    private var scrollingSeparator: View
    private var buttonsWrapper: LinearLayout
    private var noticeTextView: TextView

    private var additionalContentView: View? = null

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

    init {
        setView(view)
        dialogContainer = view.findViewById(R.id.dialog_container)
        titleTextView = view.findViewById(R.id.titleTextView)
        messageTextView = view.findViewById(R.id.messageTextView)
        contentView = view.findViewById(R.id.content_view)
        scrollingSeparator = view.findViewById(R.id.scrolling_separator)
        buttonsWrapper = view.findViewById(R.id.buttons_wrapper)
        noticeTextView = view.findViewById(R.id.notice_text_view)
        dialogContainer.clipChildren = true
        dialogContainer.clipToOutline = true
    }

    override fun setTitle(title: CharSequence?) {
        if (title != null) {
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
        if (message != null) {
            messageTextView.visibility = View.VISIBLE
        } else {
            messageTextView.visibility = View.GONE
        }
        messageTextView.text = message
    }

    fun setMessage(messageId: Int) {
        setMessage(context.getString(messageId))
    }

    fun setNotice(notice: CharSequence?) {
        if (notice != null) {
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

    private fun updateButtonLayout() {
        if (isScrollingLayout) {
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

    fun addButton(stringRes: Int, isPrimary: Boolean, isDestructive: Boolean = false, function: ((HabiticaAlertDialog, Int) -> Unit)? = null): Button {
        return addButton(context.getString(stringRes), isPrimary, isDestructive, function)
    }

    fun addButton(string: String, isPrimary: Boolean, isDestructive: Boolean = false, function: ((HabiticaAlertDialog, Int) -> Unit)? = null): Button {
        val button: Button = if (isPrimary) {
            if (isDestructive) {
                buttonsWrapper.inflate(R.layout.dialog_habitica_primary_destructive_button) as Button
            } else {
                buttonsWrapper.inflate(R.layout.dialog_habitica_primary_button) as Button
            }
        } else {
            val button = buttonsWrapper.inflate(R.layout.dialog_habitica_secondary_button) as Button
            if (isDestructive) {
                button.setTextColor(ContextCompat.getColor(context, R.color.red_100))
            }
            button
        }
        button.text = string
        button.minWidth = 147.dpToPx(context)
        button.setScaledPadding(context, 20, 0, 20, 0)
        return addButton(button, function) as Button
    }


    fun addButton(buttonView: View, function: ((HabiticaAlertDialog, Int) -> Unit)? = null): View {
        val weakThis = WeakReference<HabiticaAlertDialog>(this)
        val buttonIndex = buttonsWrapper.childCount
        buttonView.setOnClickListener {
            weakThis.get()?.let { it1 ->
                if (function != null) {
                    function(it1, buttonIndex)
                }
                dismiss()
            }
        }
        configureButtonLayoutParams(buttonView)
        buttonsWrapper.addView(buttonView)
        return buttonView
    }

    private fun configureButtonLayoutParams(buttonView: View) {
        val layoutParams = if (isScrollingLayout) {
            val params = LinearLayout.LayoutParams(0, 38.dpToPx(context))
            params.weight = 1f
            params
        } else {
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 38.dpToPx(context))
        }
        buttonView.layoutParams = layoutParams
        buttonView.elevation = 10f
    }
}