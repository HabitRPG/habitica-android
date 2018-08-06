package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.layoutInflater

open class HabiticaAlertDialog(context: Context) : AlertDialog(context) {

    private val view: LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as LinearLayout
    private var titleTextView: TextView
    private var subtitleTextView: TextView
    private var messageTextView: TextView

    private var additionalContentView: View? = null

    init {
        setView(view)
        titleTextView = view.findViewById(R.id.titleTextView)
        subtitleTextView = view.findViewById(R.id.subtitleTextView)
        messageTextView = view.findViewById(R.id.messageTextView)
    }

    override fun setTitle(title: CharSequence?) {
        titleTextView.text = title
    }

    override fun setTitle(titleId: Int) {
        setTitle(context.getString(titleId))
    }

    fun setTitleBackground(colorId: Int) {
        titleTextView.setBackgroundColor(ContextCompat.getColor(context, colorId))
    }

    fun setTitleBackgroundColor(color: Int) {
        titleTextView.setBackgroundColor(color)
    }

    fun setSubtitle(subtitle: CharSequence?) {
        if (subtitle != null) {
            subtitleTextView.visibility = View.VISIBLE
        } else {
            subtitleTextView.visibility = View.GONE
        }
        subtitleTextView.text = subtitle
    }

    fun setSubtitle(subtitleId: Int) {
        setSubtitle(context.getString(subtitleId))
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

    fun setAdditionalContentView(view: View?, index: Int = -1) {
        this.view.removeView(additionalContentView)
        additionalContentView = view
        if (index >= 0) {
            this.view.addView(view, index)
        } else {
            this.view.addView(view)
        }
    }

    fun getContentView(): View? = additionalContentView
}