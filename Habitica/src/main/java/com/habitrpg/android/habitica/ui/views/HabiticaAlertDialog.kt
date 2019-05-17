package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.extensions.setScaledPadding
import java.lang.ref.WeakReference

open class HabiticaAlertDialog(context: Context) : AlertDialog(context) {

    private val view: LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as LinearLayout
    private var titleTextView: TextView
    private var messageTextView: TextView
    private var contentView: ViewGroup
    private var buttonsWrapper: LinearLayout

    private var additionalContentView: View? = null

    init {
        setView(view)
        titleTextView = view.findViewById(R.id.titleTextView)
        messageTextView = view.findViewById(R.id.messageTextView)
        contentView = view.findViewById(R.id.content_view)
        buttonsWrapper = view.findViewById(R.id.buttons_wrapper)
    }

    override fun setTitle(title: CharSequence?) {
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



    fun getContentView(): View? = additionalContentView

    fun addButton(stringRes: Int, isPrimary: Boolean, function: (HabiticaAlertDialog) -> Unit) {
        val button = Button(context)
        button.text = context.getString(stringRes)
        button.transformationMethod = null
        button.textSize = context.resources.getDimension(R.dimen.button_text_size)
        if (isPrimary) {
            button.background = context.getDrawable(R.drawable.button_background_primary)
            button.setTextColor(context.getThemeColor(R.attr.textColorPrimaryDark))
        } else {
            button.background = ColorDrawable(ContextCompat.getColor(context, R.color.transparent))
            button.setTextColor(ContextCompat.getColor(context, R.color.brand_400))
        }
        val weakThis = WeakReference<HabiticaAlertDialog>(this)
        button.setOnClickListener { weakThis.get()?.let { it1 -> function(it1) } }
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 38.dpToPx(context))
        button.setScaledPadding(context, 26, 0, 26, 0)
        button.minWidth = 147.dpToPx(context)
        button.layoutParams = layoutParams
        buttonsWrapper.addView(button)
    }
}