package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.TaskTappedEvent
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindColor
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.EllipsisTextView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

abstract class BaseTaskViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


    var task: Task? = null
    var errorButtonClicked: Action? = null
    protected var context: Context
    private val titleTextView: EllipsisTextView by bindView(itemView, R.id.checkedTextView)
    private val notesTextView: EllipsisTextView? by bindView(itemView, R.id.notesTextView)
    internal val rightBorderView: View? by bindOptionalView(itemView, R.id.rightBorderView)
    protected val specialTaskTextView: TextView? by bindOptionalView(itemView, R.id.specialTaskText)
    private val iconViewChallenge: ImageView? by bindView(itemView, R.id.iconviewChallenge)
    private val iconViewReminder: ImageView? by bindOptionalView(itemView, R.id.iconviewReminder)
    private val iconViewTag: ImageView? by bindView(itemView, R.id.iconviewTag)
    private val taskIconWrapper: LinearLayout? by bindView(itemView, R.id.taskIconWrapper)
    private val approvalRequiredTextView: TextView? by bindView(itemView, R.id.approvalRequiredTextField)
    private val expandNotesButton: Button? by bindOptionalView(R.id.expand_notes_button)
    private val syncingView: ProgressBar? by bindOptionalView(R.id.syncing_view)
    private val errorIconView: ImageButton? by bindOptionalView(R.id.error_icon)
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

        errorIconView?.setOnClickListener { errorButtonClicked?.run()}

        //Re enable when we find a way to only react when a link is tapped.
        //notesTextView.movementMethod = LinkMovementMethod.getInstance()
        //titleTextView.movementMethod = LinkMovementMethod.getInstance()

        expandNotesButton?.setOnClickListener { expandTask() }
        notesTextView?.addEllipsesListener(object : EllipsisTextView.EllipsisListener {
            override fun ellipsisStateChanged(ellipses: Boolean) {
                Single.just(ellipses)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer{ hasEllipses ->
                            expandNotesButton?.visibility = if (hasEllipses || notesExpanded) View.VISIBLE else View.GONE
                        }, RxErrorHandler.handleEmptyError())

            }
        })
        context = itemView.context
    }

    private fun expandTask() {
        notesExpanded = !notesExpanded
        if (notesExpanded) {
            notesTextView?.maxLines = 100
            expandNotesButton?.text = context.getString(R.string.collapse_notes)
        } else {
            notesTextView?.maxLines = 3
            expandNotesButton?.text = context.getString(R.string.expand_notes)
        }
    }

    open fun bindHolder(newTask: Task, position: Int) {
        task = newTask
        itemView.setBackgroundResource(R.color.white)

        expandNotesButton?.visibility = View.GONE
        if (newTask.notes?.isNotEmpty() == true) {
            notesTextView?.visibility = View.VISIBLE
            //expandNotesButton.visibility = if (notesTextView.hadEllipses() || notesExpanded) View.VISIBLE else View.GONE
        } else {
            notesTextView?.visibility = View.GONE
        }

        if (canContainMarkdown()) {
            if (newTask.parsedText != null) {
                titleTextView.text = newTask.parsedText
            } else {
                titleTextView.text = newTask.text
                if (newTask.text.isNotEmpty()) {
                    Single.just(newTask.text)
                            .map { MarkdownParser.parseMarkdown(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer{ parsedText ->
                                newTask.parsedText = parsedText
                                titleTextView.text = parsedText
                            }, RxErrorHandler.handleEmptyError())
                }
            if (newTask.parsedNotes != null) {
                notesTextView?.text = newTask.parsedNotes
            } else {
                notesTextView?.text = newTask.notes
                newTask.notes.notNull {notes ->
                    if (notes.isEmpty()) {
                        return@notNull
                    }
                    Single.just(notes)
                            .map { MarkdownParser.parseMarkdown(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer { parsedNotes ->
                                newTask.parsedNotes = parsedNotes
                                notesTextView?.text = parsedNotes
                            }, RxErrorHandler.handleEmptyError())
                }
            }
            }
        } else {
            titleTextView.text = newTask.text
            notesTextView?.text = newTask.notes
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

        syncingView?.visibility = if (task?.isSaving == true) View.VISIBLE else View.GONE
        errorIconView?.visibility = if (task?.hasErrored == true) View.VISIBLE else View.GONE
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
