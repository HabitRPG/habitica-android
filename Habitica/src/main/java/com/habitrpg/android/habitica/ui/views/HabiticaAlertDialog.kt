package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.extensions.layoutInflater

open class HabiticaAlertDialog(context: Context) : AlertDialog(context) {

    private val view: LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as LinearLayout
    private val titleTextView: TextView by bindView(view, R.id.titleTextView)
    private val subtitleTextView: TextView by bindView(view, R.id.subtitleTextView)
    private val messageTextView: TextView by bindView(view, R.id.messageTextView)
    private val contentViewContainer: ViewGroup by bindView(view, R.id.contentViewContainer)

    init {
        setView(view)
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
        setAdditionalContentView(inflater.inflate(layoutResID, contentViewContainer, false))
    }

    fun setAdditionalContentView(view: View?) {
        contentViewContainer.removeAllViewsInLayout()
        if (view != null) {
            contentViewContainer.visibility = View.VISIBLE
        } else {
            contentViewContainer.visibility = View.GONE
        }
        contentViewContainer.addView(view)
    }

    fun getContentView(): View? {
        return if (contentViewContainer.childCount > 0) {
            contentViewContainer.getChildAt(0)
        } else {
            null
        }
    }
}