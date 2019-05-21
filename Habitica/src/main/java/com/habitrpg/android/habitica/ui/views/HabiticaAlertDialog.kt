package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setScaledPadding
import java.lang.ref.WeakReference

open class HabiticaAlertDialog(context: Context) : AlertDialog(context, R.style.HabiticaAlertDialogTheme) {

    private val view: LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as LinearLayout
    private var titleTextView: TextView
    private var messageTextView: TextView
    private var contentView: ViewGroup
    private var buttonsWrapper: LinearLayout
    private var noticeTextView: TextView

    private var additionalContentView: View? = null

    init {
        setView(view)
        titleTextView = view.findViewById(R.id.titleTextView)
        messageTextView = view.findViewById(R.id.messageTextView)
        contentView = view.findViewById(R.id.content_view)
        buttonsWrapper = view.findViewById(R.id.buttons_wrapper)
        noticeTextView = view.findViewById(R.id.notice_text_view)
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

    fun setAdditionalContentView(layoutResID: Int) {
        val inflater = context.layoutInflater
        setAdditionalContentView(inflater.inflate(layoutResID, view, false))
    }

    fun setAdditionalContentView(view: View?) {
        this.view.removeView(additionalContentView)
        additionalContentView = view
        this.contentView.addView(view)
        val layoutParams = view?.layoutParams
        layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        view?.layoutParams = layoutParams
        contentView.forceLayout()
    }

    fun setAdditionalContentSidePadding(padding: Int) {
        contentView.setPadding(padding, 0, padding, 0)
        contentView.requestLayout()
    }

    fun getContentView(): View? = additionalContentView

    fun addButton(stringRes: Int, isPrimary: Boolean, function: ((HabiticaAlertDialog, Int) -> Unit)? = null) {
        addButton(context.getString(stringRes), isPrimary, function)
    }

    fun addButton(string: String, isPrimary: Boolean, function: ((HabiticaAlertDialog, Int) -> Unit)? = null) {
        val button: Button = if (isPrimary) {
            buttonsWrapper.inflate(R.layout.dialog_habitica_primary_button) as Button
        } else {
            buttonsWrapper.inflate(R.layout.dialog_habitica_secondary_button) as Button
        }
        button.text = string
        val weakThis = WeakReference<HabiticaAlertDialog>(this)
        val buttonIndex = buttonsWrapper.childCount
        button.setOnClickListener {
            weakThis.get()?.let { it1 ->
                function?.invoke(it1, buttonIndex)
                dismiss()
            }
        }
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 38.dpToPx(context))
        button.setScaledPadding(context, 26, 0, 26, 0)
        button.minWidth = 147.dpToPx(context)
        button.layoutParams = layoutParams
        buttonsWrapper.addView(button)
    }
}