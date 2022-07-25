package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.android.habitica.ui.views.EllipsisTextView
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.common.habitica.helpers.setParsedMarkdown
import com.habitrpg.common.habitica.models.responses.TaskDirection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

abstract class BaseTaskViewHolder constructor(
    itemView: View,
    var scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    var openTaskFunc: ((Pair<Task, View>) -> Unit),
    var brokenTaskFunc: ((Task) -> Unit)
) : BindableViewHolder<Task>(itemView), View.OnTouchListener {
    private val scope = MainScope()
    var task: Task? = null
    var movingFromPosition: Int? = null
    var errorButtonClicked: Action? = null
    var isLocked = false
    protected var context: Context
    private val mainTaskWrapper: ViewGroup = itemView.findViewById(R.id.main_task_wrapper)
    protected val titleTextView: EllipsisTextView = itemView.findViewById(R.id.checkedTextView)
    protected val notesTextView: EllipsisTextView? = itemView.findViewById(R.id.notesTextView)
    protected val calendarIconView: ImageView? = itemView.findViewById(R.id.iconViewCalendar)
    protected val iconViewTeam: ImageView? = itemView.findViewById(R.id.iconViewTeamTask)
    protected val specialTaskTextView: TextView? = itemView.findViewById(R.id.specialTaskText)
    private val iconViewChallenge: ImageView? = itemView.findViewById(R.id.iconviewChallenge)
    private val iconViewReminder: ImageView? = itemView.findViewById(R.id.iconviewReminder)
    private val taskIconWrapper: LinearLayout? = itemView.findViewById(R.id.taskIconWrapper)
    private val approvalRequiredTextView: TextView? = itemView.findViewById(R.id.approvalRequiredTextField)
    private val expandNotesButton: Button? = itemView.findViewById(R.id.expand_notes_button)
    private val syncingView: ProgressBar? = itemView.findViewById(R.id.syncing_view)
    private val errorIconView: ImageButton? = itemView.findViewById(R.id.error_icon)
    protected val taskGray: Int = ContextCompat.getColor(itemView.context, R.color.offset_background)
    protected val streakIconView: ImageView = itemView.findViewById(R.id.iconViewStreak)
    protected val streakTextView: TextView = itemView.findViewById(R.id.streakTextView)
    protected val reminderTextView: TextView = itemView.findViewById(R.id.reminder_textview)

    private var openTaskDisabled: Boolean = false
    private var taskActionsDisabled: Boolean = false
    private var notesExpanded = false

    protected open val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible = false

            if (iconViewTeam?.visibility == View.VISIBLE) {
                isVisible = true
            }
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
            if (this.streakTextView.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    init {
        itemView.setOnTouchListener(this)
        itemView.isClickable = true
        mainTaskWrapper.clipToOutline = true

        titleTextView.setOnClickListener { onTouch(it, null) }
        notesTextView?.setOnClickListener { onTouch(it, null) }
        errorIconView?.setOnClickListener { errorButtonClicked?.run() }

        notesTextView?.movementMethod = LinkMovementMethod.getInstance()
        titleTextView.movementMethod = LinkMovementMethod.getInstance()

        expandNotesButton?.setOnClickListener { expandTask() }
        iconViewChallenge?.setOnClickListener {
            task?.let { t ->
                if (task?.challengeBroken?.isNotBlank() == true) brokenTaskFunc(t)
            }
        }
        notesTextView?.addEllipsesListener(object : EllipsisTextView.EllipsisListener {
            override fun ellipsisStateChanged(ellipses: Boolean) {
                scope.launch(Dispatchers.Main.immediate) {
                    if (ellipses && notesTextView.maxLines != 3) {
                        notesTextView.maxLines = 3
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
            notesTextView?.maxLines = 8
            expandNotesButton?.text = context.getString(R.string.expand_notes)
        }
    }

    override fun bind(data: Task, position: Int, displayMode: String) {
        notesExpanded = false
        task = data
        itemView.setBackgroundColor(context.getThemeColor(R.attr.colorContentBackground))

        expandNotesButton?.visibility = View.GONE
        notesExpanded = false
        notesTextView?.maxLines = 8
        if (data.notes?.isNotEmpty() == true) {
            notesTextView?.visibility = View.VISIBLE
            notesTextView?.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))
            // expandNotesButton.visibility = if (notesTextView.hadEllipses() || notesExpanded) View.VISIBLE else View.GONE
        } else {
            notesTextView?.visibility = View.GONE
        }

        if (canContainMarkdown()) {
            if (data.parsedText != null) {
                titleTextView.setParsedMarkdown(data.parsedText)
            } else if (MarkdownParser.hasCached(data.text)) {
                titleTextView.setParsedMarkdown(MarkdownParser.parseMarkdown(data.text))
            } else {
                titleTextView.text = data.text
                if (data.text.isNotEmpty()) {
                    Single.just(data.text)
                        .map { MarkdownParser.parseMarkdown(it) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { parsedText ->
                                data.parsedText = parsedText
                                titleTextView.setParsedMarkdown(parsedText)
                            },
                            RxErrorHandler.handleEmptyError()
                        )
                }
            }
            if (displayMode != "minimal") {
                when {
                    data.parsedNotes != null -> {
                        notesTextView?.setParsedMarkdown(data.parsedNotes)
                    }
                    MarkdownParser.hasCached(data.notes) -> {
                        notesTextView?.setParsedMarkdown(MarkdownParser.parseMarkdown(data.notes))
                    }
                    else -> {
                        notesTextView?.text = data.notes
                        data.notes?.let { notes ->
                            if (notes.isEmpty()) {
                                return@let
                            }
                            Single.just(notes)
                                .map { MarkdownParser.parseMarkdown(it) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    { parsedNotes ->
                                        data.parsedNotes = parsedNotes
                                        notesTextView?.setParsedMarkdown(parsedNotes)
                                    },
                                    RxErrorHandler.handleEmptyError()
                                )
                        }
                    }
                }
            } else {
                notesTextView?.visibility = View.GONE
            }
        } else {
            titleTextView.text = data.text
            if (displayMode != "minimal") {
                notesTextView?.text = data.notes
            } else {
                notesTextView?.visibility = View.GONE
            }
        }
        titleTextView.setTextColor(ContextCompat.getColor(context, R.color.text_primary))

        if (displayMode == "standard") {
            iconViewReminder?.visibility = if ((data.reminders?.size ?: 0) > 0) View.VISIBLE else View.GONE

            iconViewChallenge?.visibility = if (task?.challengeID != null) View.VISIBLE else View.GONE
            if (task?.challengeID != null) {
                if (task?.challengeBroken?.isNotBlank() == true) {
                    iconViewChallenge?.alpha = 1.0f
                    iconViewChallenge?.imageTintList = ContextCompat.getColorStateList(context, R.color.white)
                    iconViewChallenge?.setImageResource(R.drawable.task_broken_megaphone)
                } else {
                    iconViewChallenge?.alpha = 0.3f
                    iconViewChallenge?.imageTintList = ContextCompat.getColorStateList(context, R.color.text_ternary)
                    iconViewChallenge?.setImageResource(R.drawable.task_megaphone)
                }
            }
            configureSpecialTaskTextView(data)

            iconViewTeam?.visibility = if (data.isGroupTask) View.VISIBLE else View.GONE

            taskIconWrapper?.visibility = if (taskIconWrapperIsVisible) View.VISIBLE else View.GONE
        } else {
            taskIconWrapper?.visibility = View.GONE
            mainTaskWrapper.minimumHeight = 48.dpToPx(context)
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
        calendarIconView?.visibility = View.GONE
    }

    open fun onLeftActionTouched() {}
    open fun onRightActionTouched() {}

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null) {
            if (motionEvent.action != MotionEvent.ACTION_UP) return true
            if (motionEvent.y <= mainTaskWrapper.height + 5.dpToPx(context)) {
                if ((this.task?.checklist?.isNotEmpty() != true)) {
                    if (motionEvent.x <= 72.dpToPx(context)) {
                        onLeftActionTouched()
                        return true
                    } else if ((itemView.width - motionEvent.x <= 72.dpToPx(context))) {
                        onRightActionTouched()
                        return true
                    }
                } else {
                    val checkboxHolder: ViewGroup = itemView.findViewById(R.id.checkBoxHolder)
                    if (mainTaskWrapper.height == checkboxHolder.height) {
                        if (motionEvent.x <= 72.dpToPx(context)) {
                            onLeftActionTouched()
                            return true
                        } else if ((itemView.width - motionEvent.x <= 72.dpToPx(context))) {
                            onRightActionTouched()
                            return true
                        }
                    } else {
                        if (motionEvent.y <= (checkboxHolder.height + 5.dpToPx(context))) {
                            if (motionEvent.x <= 72.dpToPx(context)) {
                                onLeftActionTouched()
                                return true
                            } else if ((itemView.width - motionEvent.x <= 72.dpToPx(context))) {
                                onRightActionTouched()
                                return true
                            }
                        }
                    }
                }
            }
        }
        task?.let { openTaskFunc(Pair(it, mainTaskWrapper)) }
        return true
    }

    open fun canContainMarkdown(): Boolean {
        return true
    }

    open fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        this.openTaskDisabled = openTaskDisabled
        this.taskActionsDisabled = taskActionsDisabled
    }
}
