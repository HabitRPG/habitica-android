package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView

/**
 * Created by phillip on 01.02.18.
 */

open class HabiticaAlertDialog(context: Context) : AlertDialog(context) {

    private val view: LinearLayout = LayoutInflater.from(context).inflate(R.layout.dialog_habitica_base, null) as LinearLayout
    private val titleTextView: TextView by bindView(view, R.id.titleTextView)
    private val subtitleTextView: TextView by bindView(view, R.id.subtitleTextView)
    private val messageTextView: TextView by bindView(view, R.id.messageTextView)

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
}