package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.TaskTappedEvent
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindColor
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.views.EllipsisTextView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import net.pherth.android.emoji_library.EmojiTextView
import org.greenrobot.eventbus.EventBus

abstract class BaseTaskViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


    var task: Task? = null
    protected var context: Context
    private val titleTextView: EllipsisTextView by bindView(itemView, R.id.checkedTextView)
    private val notesTextView: EllipsisTextView by bindView(itemView, R.id.notesTextView)
    internal val rightBorderView: View? by bindOptionalView(itemView, R.id.rightBorderView)
    protected val specialTaskTextView: TextView? by bindOptionalView(itemView, R.id.specialTaskText)
    private val iconViewChallenge: ImageView? by bindView(itemView, R.id.iconviewChallenge)
    private val iconViewReminder: ImageView? by bindOptionalView(itemView, R.id.iconviewReminder)
    private val iconViewTag: ImageView? by bindView(itemView, R.id.iconviewTag)
    private val taskIconWrapper: LinearLayout? by bindView(itemView, R.id.taskIconWrapper)
    private val approvalRequiredTextView: TextView? by bindView(itemView, R.id.approvalRequiredTextField)
    private val expandNotesButton: Button by bindView(R.id.expand_notes_button)
    protected val taskGray: Int by bindColor(itemView.context, R.color.task_gray)

    private var openTaskDisabled: Boolean = false
    private var taskActionsDisabled: Boolean = false
    private var notesExpanded = false

    protected open val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible = false

            if (iconViewReminder?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (iconViewTag?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (iconViewChallenge?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (iconViewReminder?.visibility == View.VISIBLE) {
                isVisible = true
            }
            if (specialTaskTextView?.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    init {

        itemView.setOnClickListener { onClick(it) }
        itemView.isClickable = true

        //Re enable when we find a way to only react when a link is tapped.
        //notesTextView.movementMethod = LinkMovementMethod.getInstance()
        //titleTextView.movementMethod = LinkMovementMethod.getInstance()

        expandNotesButton.setOnClickListener { expandTask() }
        notesTextView.addEllipsesListener(object : EllipsisTextView.EllipsisListener {
            override fun ellipsisStateChanged(ellipses: Boolean) {
                Single.just(ellipses)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer{ hasEllipses ->
                            expandNotesButton.visibility = if (hasEllipses || notesExpanded) View.VISIBLE else View.GONE
                        }, RxErrorHandler.handleEmptyError())

            }
        })
        context = itemView.context
    }

    private fun expandTask() {
        notesExpanded = !notesExpanded
        if (notesExpanded) {
            notesTextView.maxLines = 100
            expandNotesButton.text = context.getString(R.string.collapse_notes)
        } else {
            notesTextView.maxLines = 3
            expandNotesButton.text = context.getString(R.string.expand_notes)
        }
    }

    open fun bindHolder(newTask: Task, position: Int) {
        task = newTask
        itemView.setBackgroundResource(R.color.white)

        if (newTask.notes?.isNotEmpty() == true) {
            notesTextView.visibility = View.VISIBLE
            //expandNotesButton.visibility = if (notesTextView.hadEllipses() || notesExpanded) View.VISIBLE else View.GONE
        } else {
            notesTextView.visibility = View.GONE
            expandNotesButton.visibility = View.GONE
        }

        if (canContainMarkdown()) {
            if (newTask.parsedText != null) {
                titleTextView.text = newTask.parsedText
                notesTextView.text = newTask.parsedNotes
            } else {
                titleTextView.text = newTask.text
                notesTextView.text = newTask.notes
                Single.just(newTask.text)
                        .map { MarkdownParser.parseMarkdown(it) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer{ parsedText ->
                            newTask.parsedText = parsedText
                            titleTextView.text = newTask.parsedText
                        }, RxErrorHandler.handleEmptyError())
                newTask.notes.notNull {notes ->
                    Single.just(notes)
                            .map { MarkdownParser.parseMarkdown(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer { parsedNotes ->
                                newTask.parsedNotes = parsedNotes
                                notesTextView.text = newTask.parsedNotes
                            }, RxErrorHandler.handleEmptyError())
                }

            }
        } else {
            titleTextView.text = newTask.text
            notesTextView.text = newTask.notes
        }

        rightBorderView?.setBackgroundResource(newTask.lightTaskColor)
        iconViewReminder?.visibility = if (newTask.reminders?.size ?: 0 > 0) View.VISIBLE else View.GONE
        iconViewTag?.visibility = if (newTask.tags?.size ?: 0 > 0) View.VISIBLE else View.GONE

        iconViewChallenge?.visibility = View.GONE

        configureSpecialTaskTextView(newTask)

        taskIconWrapper?.visibility = if (taskIconWrapperIsVisible) View.VISIBLE else View.GONE

        if (newTask.isPendingApproval) {
            approvalRequiredTextView?.visibility = View.VISIBLE
        } else {
            approvalRequiredTextView?.visibility = View.GONE
        }

    }


    protected open fun configureSpecialTaskTextView(task: Task) {
        specialTaskTextView?.visibility = View.INVISIBLE
    }

    override fun onClick(v: View) {
        if (v != itemView || openTaskDisabled) {
            return
        }

        val event = TaskTappedEvent()
        event.Task = task

        EventBus.getDefault().post(event)
    }

    open fun canContainMarkdown(): Boolean {
        return true
    }

    open fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        this.openTaskDisabled = openTaskDisabled
        this.taskActionsDisabled = taskActionsDisabled
    }
}
