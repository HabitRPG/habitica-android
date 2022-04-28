package com.habitrpg.android.habitica.ui.views.tasks

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.databinding.DialogTaskFilterBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskType
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import io.reactivex.rxjava3.core.Observable
import java.util.UUID
import javax.inject.Inject

class TaskFilterDialog(context: Context, component: UserComponent?) : HabiticaBottomSheetDialog(context), RadioGroup.OnCheckedChangeListener {
    private val binding = DialogTaskFilterBinding.inflate(layoutInflater)

    @Inject
    lateinit var repository: TagRepository

    private var taskType: TaskType? = null
    private var listener: OnFilterCompletedListener? = null

    private var filterType: String? = null
    private var tags = mutableListOf<Tag>()
    private var activeTags = mutableListOf<String>()
    private val editedTags = HashMap<String, Tag>()
    private val createdTags = HashMap<String, Tag>()
    private val deletedTags = ArrayList<String>()

    private val addIcon: Drawable?
    private var isEditing: Boolean = false

    init {
        component?.inject(this)
        addIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_purple_300_36dp)

        setTitle(R.string.filters)
        this.setContentView(binding.root)

        binding.taskFilterWrapper.setOnCheckedChangeListener(this)

        binding.clearButton.setOnClickListener {
            if (isEditing) {
                stopEditing()
            }
            setActiveFilter(null)
            setActiveTags(null)
        }

        binding.tagEditButton.setOnClickListener { editButtonClicked() }
    }

    override fun dismiss() {
        listener?.onFilterCompleted(filterType, activeTags)
        super.dismiss()
    }

    override fun show() {
        super.show()
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    fun setTags(tags: List<Tag>) {
        this.tags = repository.getUnmanagedCopy(tags).toMutableList()
        createTagViews()
    }

    private fun createTagViews() {
        binding.tagsList.removeAllViews()
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), // disabled
                intArrayOf(android.R.attr.state_checked) // enabled
            ),
            intArrayOf(
                Color.LTGRAY, // disabled
                context.getThemeColor(R.attr.colorAccent) // enabled
            )
        )
        val leftPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
        val verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
        for (tag in tags) {
            val tagCheckbox = AppCompatCheckBox(context)
            tagCheckbox.text = tag.name
            tagCheckbox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tagCheckbox.isChecked = activeTags.contains(tag.id)
            tagCheckbox.setPadding(
                tagCheckbox.paddingLeft + leftPadding,
                verticalPadding,
                tagCheckbox.paddingRight,
                verticalPadding
            )
            tagCheckbox.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            CompoundButtonCompat.setButtonTintList(tagCheckbox, colorStateList)
            tagCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!activeTags.contains(tag.id)) {
                        activeTags.add(tag.id)
                    }
                } else {
                    if (activeTags.contains(tag.id)) {
                        activeTags.remove(tag.id)
                    }
                }
                filtersChanged()
            }
            binding.tagsList.addView(tagCheckbox)
        }
        createAddTagButton()
    }

    private fun createAddTagButton() {
        val button = Button(context)
        button.setText(R.string.add_tag)
        button.setOnClickListener { createTag() }
        button.setCompoundDrawablesWithIntrinsicBounds(addIcon, null, null, null)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            button.compoundDrawableTintList = ColorStateList.valueOf(context.getThemeColor(R.attr.colorPrimary))
        }
        button.setBackgroundResource(R.drawable.layout_rounded_bg_lighter_gray)
        button.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        binding.tagsList.addView(button)
    }

    private fun createTag() {
        val tag = Tag()
        tag.id = UUID.randomUUID().toString()
        tags.add(tag)
        createdTags[tag.id] = tag
        startEditing()
    }

    private fun startEditing() {
        isEditing = true
        binding.tagsList.removeAllViews()
        createTagEditViews()
        binding.tagEditButton.setText(R.string.done)
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private fun stopEditing() {
        isEditing = false
        binding.tagsList.removeAllViews()
        createTagViews()
        binding.tagEditButton.setText(R.string.edit_tag_btn_edit)
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        repository.updateTags(editedTags.values).toObservable().flatMap { tags -> Observable.fromIterable(tags) }.subscribe({ tag -> editedTags.remove(tag.id) }, RxErrorHandler.handleEmptyError())
        repository.createTags(createdTags.values).toObservable().flatMap { tags -> Observable.fromIterable(tags) }.subscribe({ tag -> createdTags.remove(tag.id) }, RxErrorHandler.handleEmptyError())
        repository.deleteTags(deletedTags).subscribe({ deletedTags.clear() }, RxErrorHandler.handleEmptyError())
    }

    private fun createTagEditViews() {
        val inflater = LayoutInflater.from(context)
        for (index in tags.indices) {
            val tag = tags[index]
            createTagEditView(inflater, index, tag)
        }
        createAddTagButton()
    }

    private fun createTagEditView(inflater: LayoutInflater, index: Int, tag: Tag) {
        val wrapper = inflater.inflate(R.layout.edit_tag_item, binding.tagsList, false) as? LinearLayout
        val tagEditText = wrapper?.findViewById<View>(R.id.edit_text) as? EditText
        tagEditText?.setText(tag.name)
        tagEditText?.addTextChangedListener(
            OnChangeTextWatcher { s, _, _, _ ->
                if (index >= tags.size) {
                    return@OnChangeTextWatcher
                }
                val changedTag = tags[index]
                changedTag.name = s.toString()
                if (createdTags.containsKey(changedTag.id)) {
                    createdTags[changedTag.id] = changedTag
                } else {
                    editedTags[changedTag.id] = changedTag
                }
                tags[index] = changedTag
            }
        )
        val deleteButton = wrapper?.findViewById<View>(R.id.delete_button) as? ImageButton
        deleteButton?.setOnClickListener {
            deletedTags.add(tag.id)
            if (createdTags.containsKey(tag.id)) {
                createdTags.remove(tag.id)
            }
            if (editedTags.containsKey(tag.id)) {
                editedTags.remove(tag.id)
            }
            activeTags.remove(tag.id)
            tags.remove(tag)
            binding.tagsList.removeView(wrapper)
        }
        binding.tagsList.addView(wrapper)
    }

    fun setActiveTags(tagIds: MutableList<String>?) {
        if (tagIds == null) {
            this.activeTags.clear()
        } else {
            this.activeTags = tagIds
        }
        for (index in 0 until binding.tagsList.childCount - 1) {
            (binding.tagsList.getChildAt(index) as? AppCompatCheckBox)?.isChecked = false
        }
        for (tagId in this.activeTags) {
            val index = indexForId(tagId)
            if (index >= 0) {
                (binding.tagsList.getChildAt(index) as? AppCompatCheckBox)?.isChecked = true
            }
        }
        filtersChanged()
    }

    private fun indexForId(tagId: String): Int {
        for (index in tags.indices) {
            if (tagId == tags[index].id) {
                return index
            }
        }
        return -1
    }

    fun setTaskType(taskType: TaskType, activeFilter: String?) {
        this.taskType = taskType
        when (taskType) {
            TaskType.HABIT -> {
                binding.taskTypeTitle.setText(R.string.habits)
                binding.allTaskFilter.setText(R.string.all)
                binding.secondTaskFilter.setText(R.string.weak)
                binding.thirdTaskFilter.setText(R.string.strong)
            }
            TaskType.DAILY -> {
                binding.taskTypeTitle.setText(R.string.dailies)
                binding.allTaskFilter.setText(R.string.all)
                binding.secondTaskFilter.setText(R.string.due)
                binding.thirdTaskFilter.setText(R.string.gray)
            }
            TaskType.TODO -> {
                binding.taskTypeTitle.setText(R.string.todos)
                binding.allTaskFilter.setText(R.string.active)
                binding.secondTaskFilter.setText(R.string.dated)
                binding.thirdTaskFilter.setText(R.string.completed)
            }
        }
        setActiveFilter(activeFilter)
    }

    private fun setActiveFilter(activeFilter: String?) {
        filterType = activeFilter
        var checkedId = -1
        if (activeFilter == null) {
            checkedId = R.id.all_task_filter
        } else {
            when (activeFilter) {
                Task.FILTER_ALL -> checkedId = R.id.all_task_filter
                Task.FILTER_WEAK, Task.FILTER_DATED -> checkedId = R.id.second_task_filter
                Task.FILTER_STRONG, Task.FILTER_GRAY, Task.FILTER_COMPLETED -> checkedId = R.id.third_task_filter
                Task.FILTER_ACTIVE -> checkedId = if (taskType == TaskType.DAILY) {
                    R.id.second_task_filter
                } else {
                    R.id.all_task_filter
                }
            }
        }
        binding.taskFilterWrapper.check(checkedId)
        filtersChanged()
    }

    override fun onCheckedChanged(group: RadioGroup, @IdRes checkedId: Int) {
        if (taskType == null) {
            return
        }
        when (checkedId) {
            R.id.all_task_filter -> filterType = if (taskType != TaskType.TODO) {
                Task.FILTER_ALL
            } else {
                Task.FILTER_ACTIVE
            }
            R.id.second_task_filter -> when (taskType) {
                TaskType.HABIT -> filterType = Task.FILTER_WEAK
                TaskType.DAILY -> filterType = Task.FILTER_ACTIVE
                TaskType.TODO -> filterType = Task.FILTER_DATED
            }
            R.id.third_task_filter -> when (taskType) {
                TaskType.HABIT -> filterType = Task.FILTER_STRONG
                TaskType.DAILY -> filterType = Task.FILTER_GRAY
                TaskType.TODO -> filterType = Task.FILTER_COMPLETED
            }
        }
        filtersChanged()
    }

    private fun editButtonClicked() {
        isEditing = !isEditing
        if (isEditing) {
            startEditing()
        } else {
            stopEditing()
        }
    }

    private fun filtersChanged() {
        binding.clearButton.isEnabled = hasActiveFilters()
        binding.clearButton.setTextColor(
            if (binding.clearButton.isEnabled) {
                context.getThemeColor(R.attr.colorAccent)
            } else {
                ContextCompat.getColor(context, R.color.text_dimmed)
            }
        )
    }

    private fun hasActiveFilters(): Boolean {
        return binding.taskFilterWrapper.checkedRadioButtonId != R.id.all_task_filter || activeTags.size > 0
    }

    fun setListener(listener: OnFilterCompletedListener) {
        this.listener = listener
    }

    interface OnFilterCompletedListener {
        fun onFilterCompleted(activeTaskFilter: String?, activeTags: MutableList<String>)
    }
}
