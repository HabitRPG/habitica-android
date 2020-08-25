package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.*
import com.habitrpg.android.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.android.habitica.ui.views.EllipsisTextView
import io.noties.markwon.utils.NoCopySpannableFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class BaseTaskViewHolder constructor(itemView: View, var scoreTaskFunc: ((Task, TaskDirection) -> Unit), var openTaskFunc: ((Task) -> Unit), var brokenTaskFunc: ((Task) -> Unit)) : BindableViewHolder<Task>(itemView), View.OnClickListener {


    var task: Task? = null
    var movingFromPosition: Int? = null
    var errorButtonClicked: Action? = null
    protected var context: Context
    private val mainTaskWrapper: ViewGroup by bindView(itemView, R.id.main_task_wrapper)
    private val titleTextView: EllipsisTextView by bindView(itemView, R.id.checkedTextView)
    private val notesTextView: EllipsisTextView? by bindView(itemView, R.id.notesTextView)
    protected val specialTaskTextView: TextView? by bindOptionalView(itemView, R.id.specialTaskText)
    private val iconViewChallenge: ImageView? by bindView(itemView, R.id.iconviewChallenge)
    private val iconViewReminder: ImageView? by bindOptionalView(itemView, R.id.iconviewReminder)
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
        mainTaskWrapper.clipToOutline = true

        titleTextView.setOnClickListener { onClick(it) }
        notesTextView?.setOnClickListener { onClick(it) }
        errorIconView?.setOnClickListener { errorButtonClicked?.run()}

        //Re enable when we find a way to only react when a link is tapped.
        //notesTextView.movementMethod = LinkMovementMethod.getInstance()
        //titleTextView.movementMethod = LinkMovementMethod.getInstance()

        expandNotesButton?.setOnClickListener { expandTask() }
        iconViewChallenge?.setOnClickListener {
            task?.let { t ->
                if (task?.challengeBroken?.isNotBlank() == true) brokenTaskFunc(t)
            }
        }
        notesTextView?.addEllipsesListener(object : EllipsisTextView.EllipsisListener {
            override fun ellipsisStateChanged(ellipses: Boolean) {
                GlobalScope.launch(Dispatchers.Main.immediate) {
                    if (ellipses && notesTextView?.maxLines != 3) {
                        notesTextView?.maxLines = 3
                    }
                    expandNotesButton?.visibility = if (ellipses || notesExpanded) View.VISIBLE else View.GONE
                }
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
            notesTextView?.maxLines = 5
            expandNotesButton?.text = context.getString(R.string.expand_notes)
        }
    }

    override fun bind(data: Task, position: Int, displayMode: String) {
        task = data
        itemView.setBackgroundResource(R.color.white)

        expandNotesButton?.visibility = View.GONE
        notesExpanded = false
        notesTextView?.maxLines = 5
        if (data.notes?.isNotEmpty() == true) {
            notesTextView?.visibility = View.VISIBLE
            //expandNotesButton.visibility = if (notesTextView.hadEllipses() || notesExpanded) View.VISIBLE else View.GONE
        } else {
            notesTextView?.visibility = View.GONE
        }

        if (canContainMarkdown()) {
            if (data.parsedText != null) {
                titleTextView.setParsedMarkdown(data.parsedText)
            } else {
                titleTextView.text = data.text
                titleTextView.setSpannableFactory(NoCopySpannableFactory.getInstance())
                if (data.text.isNotEmpty()) {
                    Single.just(data.text)
                            .map { MarkdownParser.parseMarkdown(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer{ parsedText ->
                                data.parsedText = parsedText
                                titleTextView.setParsedMarkdown(parsedText)
                            }, RxErrorHandler.handleEmptyError())
                }
                if (displayMode != "minimal") {
                    if (data.parsedNotes != null) {
                        notesTextView?.setParsedMarkdown(data.parsedText)
                    } else {
                        notesTextView?.text = data.notes
                        notesTextView?.setSpannableFactory(NoCopySpannableFactory.getInstance())
                        data.notes?.let {notes ->
                            if (notes.isEmpty()) {
                                return@let
                            }
                            Single.just(notes)
                                    .map { MarkdownParser.parseMarkdown(it) }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Consumer { parsedNotes ->
                                        notesTextView?.text = parsedNotes
                                        notesTextView?.setParsedMarkdown(parsedNotes)
                                    }, RxErrorHandler.handleEmptyError())
                        }
                    }
                } else {
                    notesTextView?.visibility = View.GONE
                }
            }
        } else {
            titleTextView.text = data.text
            if (displayMode != "minimal") {
                notesTextView?.text = data.notes
            } else {
                notesTextView?.visibility = View.GONE
            }
        }

        if (displayMode == "standard") {
            iconViewReminder?.visibility = if (data.reminders?.size ?: 0 > 0) View.VISIBLE else View.GONE

            iconViewChallenge?.visibility = if (task?.challengeID != null) View.VISIBLE else View.GONE
            if (task?.challengeID != null) {
                iconViewChallenge?.setImageResource(if (task?.challengeBroken?.isNotBlank() == true) R.drawable.task_broken_megaphone else R.drawable.task_megaphone)
            }
            configureSpecialTaskTextView(data)

            taskIconWrapper?.visibility = if (taskIconWrapperIsVisible) View.VISIBLE else View.GONE
        } else {
            taskIconWrapper?.visibility = View.GONE
        }


        if (data.isPendingApproval) {
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
        task?.let { openTaskFunc(it) }
    }

    open fun canContainMarkdown(): Boolean {
        return true
    }

    open fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        this.openTaskDisabled = openTaskDisabled
        this.taskActionsDisabled = taskActionsDisabled
    }
}
