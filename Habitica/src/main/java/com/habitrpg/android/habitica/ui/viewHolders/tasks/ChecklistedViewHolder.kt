package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.GroupPlanInfoProvider
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.extensionsCommon.getThemeColor
import com.habitrpg.common.habitica.extensionsCommon.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.common.habitica.helpers.setParsedMarkdown
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ChecklistedViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    var scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit),
    openTaskFunc: ((Task, View) -> Unit),
    brokenTaskFunc: ((Task) -> Unit),
    assignedTextProvider: GroupPlanInfoProvider?,
) : BaseTaskViewHolder(itemView, scoreTaskFunc, openTaskFunc, brokenTaskFunc, assignedTextProvider) {
    private val checkboxHolder: ViewGroup = itemView.findViewById(R.id.checkBoxHolder)
    private val checkmarkView: ImageView = itemView.findViewById(R.id.checkmark)
    private val lockView: ImageView = itemView.findViewById(R.id.lock_view)
    private val checkboxBackground: View = itemView.findViewById(R.id.checkBoxBackground)
    private val checklistView: LinearLayout = itemView.findViewById(R.id.checklistView)
    private val checklistIndicatorWrapper: ViewGroup = itemView.findViewById(R.id.checklistIndicatorWrapper)
    private val checklistCompletedTextView: TextView = itemView.findViewById(R.id.checkListCompletedTextView)
    private val checklistAllTextView: TextView = itemView.findViewById(R.id.checkListAllTextView)
    private val checklistDivider: View = itemView.findViewById(R.id.checklistDivider)

    init {
        checklistIndicatorWrapper.isClickable = true
        checklistIndicatorWrapper.setOnClickListener { onChecklistIndicatorClicked() }
    }

    override fun bind(
        data: Task,
        position: Int,
        displayMode: String,
        ownerID: String?,
    ) {
        var completed = data.completed(userID)
        if (data.isPendingApproval) {
            completed = false
        }
        if (isLocked) {
            this.checkmarkView.visibility = View.GONE
            this.lockView.visibility = View.VISIBLE
            val icon = AppCompatResources.getDrawable(context, R.drawable.task_lock)
            icon?.setTint(ContextCompat.getColor(context, if (data.isDue == true || data.type == TaskType.TODO) data.extraExtraDarkTaskColor else R.color.text_dimmed))
            lockView.setImageDrawable(icon)
        } else {
            this.checkmarkView.visibility = if (completed) View.VISIBLE else View.GONE
            checkmarkView.drawable.setTint(ContextCompat.getColor(context, R.color.gray_400))
            this.lockView.visibility = View.GONE
        }
        this.checklistCompletedTextView.text = data.completedChecklistCount.toString()
        this.checklistAllTextView.text = data.checklist?.size.toString()

        this.checklistView.removeAllViews()
        this.updateChecklistDisplay()

        this.checklistIndicatorWrapper.visibility = if (data.checklist?.size == 0) View.GONE else View.VISIBLE
        super.bind(data, position, displayMode, ownerID)
        val regularBoxBackground = if (task?.type == TaskType.DAILY) R.drawable.daily_unchecked else R.drawable.todo_unchecked
        val completedBoxBackground = if (task?.type == TaskType.DAILY) R.drawable.daily_checked else R.drawable.todo_checked
        val inactiveBoxBackground = R.drawable.daily_inactive
        if (this.shouldDisplayAsActive(data, userID) && !data.isPendingApproval) {
            this.checkboxHolder.setBackgroundResource(data.lightTaskColor)
            checkboxBackground.setBackgroundResource(regularBoxBackground)
        } else {
            if (completed) {
                titleTextView.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
                notesTextView?.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
                this.checkboxHolder.setBackgroundColor(context.getThemeColor(R.attr.colorWindowBackground))
                checkboxBackground.setBackgroundResource(completedBoxBackground)
            } else {
                this.checkboxHolder.setBackgroundColor(this.taskGray)
                notesTextView?.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))
                checkboxBackground.setBackgroundResource(regularBoxBackground)
                checkboxBackground.setBackgroundResource(inactiveBoxBackground)
            }
        }
    }

    abstract fun shouldDisplayAsActive(
        task: Task?,
        userID: String?,
    ): Boolean

    private fun updateChecklistDisplay() {
        // This needs to be a LinearLayout, as ListViews can not be inside other ListViews.
        if (this.shouldDisplayExpandedChecklist()) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
            if (this.task?.checklist?.isValid == true) {
                checklistView.removeAllViews()
                for (item in this.task?.checklist ?: emptyList<ChecklistItem>()) {
                    val itemView = layoutInflater?.inflate(R.layout.checklist_item_row, this.checklistView, false)
                    val checkboxBackground = itemView?.findViewById<View>(R.id.checkBoxBackground)
                    if (task?.type == TaskType.TODO) {
                        checkboxBackground?.setBackgroundResource(R.drawable.round_checklist_unchecked)
                    }
                    checkboxBackground?.backgroundTintList =
                        ContextCompat.getColorStateList(
                            context,
                            (
                                if (context.isUsingNightModeResources()) {
                                    if (task?.completed(userID) == true || (task?.type == TaskType.DAILY && task?.isDue == false)) {
                                        R.color.checkbox_fill
                                    } else {
                                        task?.lightTaskColor
                                    }
                                } else {
                                    R.color.checkbox_fill
                                }
                            ) ?: R.color.checkbox_fill,
                        )
                    val textView = itemView?.findViewById<TextView>(R.id.checkedTextView)
                    // Populate the data into the template view using the data object
                    textView?.text = item.text
                    textView?.setTextColor(ContextCompat.getColor(context, if (item.completed) R.color.text_dimmed else R.color.text_secondary))
                    if (item.text != null) {
                        MainScope().launch(Dispatchers.IO) {
                            val parsedText = MarkdownParser.parseMarkdown(item.text ?: "")
                            withContext(Dispatchers.Main) {
                                textView?.setParsedMarkdown(parsedText)
                            }
                        }
                    }
                    val checkmark = itemView?.findViewById<ImageView>(R.id.checkmark)
                    checkmark?.drawable?.setTintMode(PorterDuff.Mode.SRC_ATOP)
                    checkmark?.visibility = if (item.completed) View.VISIBLE else View.GONE
                    val checkboxHolder = itemView?.findViewById<View>(R.id.checkBoxHolder) as? ViewGroup
                    checkboxHolder?.setOnClickListener { _ ->
                        task?.let { scoreChecklistItemFunc(it, item) }
                    }
                    val color =
                        ContextCompat.getColor(
                            context,
                            if (task?.completed(userID) == true || (task?.type == TaskType.DAILY && task?.isDue == false)) {
                                checkmark?.drawable?.setTint(ContextCompat.getColor(context, R.color.text_dimmed))
                                R.color.offset_background
                            } else {
                                val color = if (context.isUsingNightModeResources()) task?.extraExtraDarkTaskColor else task?.darkTaskColor
                                checkmark?.drawable?.setTint(ContextCompat.getColor(context, color ?: R.color.text_dimmed))
                                task?.extraLightTaskColor ?: R.color.offset_background
                            },
                        )
                    color.let { checkboxHolder?.setBackgroundColor(it) }
                    this.checklistView.addView(itemView)
                }
            }
            this.checklistView.visibility = View.VISIBLE
        } else {
            this.checklistView.removeAllViewsInLayout()
            this.checklistView.visibility = View.GONE
        }
    }

    protected fun setChecklistIndicatorBackgroundActive(isActive: Boolean) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.checklist_indicator_background)
        if (isActive) {
            drawable?.setTint(ContextCompat.getColor(context, R.color.gray_200))
            val textColor =
                if (context.isUsingNightModeResources()) {
                    ContextCompat.getColor(context, R.color.gray_600)
                } else {
                    ContextCompat.getColor(context, R.color.gray_500)
                }
            checklistCompletedTextView.setTextColor(textColor)
            checklistAllTextView.setTextColor(textColor)
            checklistDivider.setBackgroundColor(textColor)
        } else {
            drawable?.setTint(ContextCompat.getColor(context, R.color.offset_background))
            val textColor = ContextCompat.getColor(context, R.color.text_quad)
            checklistCompletedTextView.setTextColor(textColor)
            checklistAllTextView.setTextColor(textColor)
            checklistDivider.setBackgroundColor(textColor)
        }
        drawable?.setTintMode(PorterDuff.Mode.MULTIPLY)
        checklistIndicatorWrapper.background = drawable
    }

    private fun onChecklistIndicatorClicked() {
        expandedChecklistRow = if (this.shouldDisplayExpandedChecklist()) null else bindingAdapterPosition
        if (this.shouldDisplayExpandedChecklist()) {
            val recyclerView = this.checklistView.parent.parent as? RecyclerView
            val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
            layoutManager?.scrollToPositionWithOffset(this.bindingAdapterPosition, 15)
        }
        updateChecklistDisplay()
    }

    override fun onLeftActionTouched() {
        super.onLeftActionTouched()
        if (task?.isValid == true && !isLocked) {
            onCheckedChanged(!(task?.completed(userID) ?: false))
        }
    }

    override fun onRightActionTouched() {
        super.onRightActionTouched()
        onChecklistIndicatorClicked()
    }

    private fun shouldDisplayExpandedChecklist(): Boolean {
        return expandedChecklistRow != null && bindingAdapterPosition == expandedChecklistRow
    }

    private fun onCheckedChanged(isChecked: Boolean) {
        if (task?.isValid != true) {
            return
        }
        if (isChecked != task?.completed(userID)) {
            task?.let { scoreTaskFunc(it, if (task?.completed(userID) == false) TaskDirection.UP else TaskDirection.DOWN) }
        }
    }

    override fun setDisabled(
        openTaskDisabled: Boolean,
        taskActionsDisabled: Boolean,
    ) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)
        this.checkboxHolder.isEnabled = !taskActionsDisabled
    }

    companion object {
        private var expandedChecklistRow: Int? = null
    }
}
