package com.habitrpg.android.habitica.ui.views.tasks

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.core.widget.TextViewCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.databinding.DialogTaskFilterBinding
import com.habitrpg.android.habitica.databinding.EditTagItemBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID
import javax.inject.Inject

class TaskFilterDialog(context: Context, component: UserComponent?) : HabiticaBottomSheetDialog(context), RadioGroup.OnCheckedChangeListener {
    lateinit var viewModel: TasksViewModel
    private val binding = DialogTaskFilterBinding.inflate(layoutInflater)

    var tagDisposale: Disposable? = null

    @Inject
    lateinit var repository: TagRepository

    var taskType: TaskType = TaskType.HABIT
        set(value) {
            field = value
            when (value) {
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
                TaskType.REWARD -> {

                }
            }
            setActiveFilter(viewModel.getActiveFilter(value))
        }

    private var tags = mutableListOf<Tag>()
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
        tagDisposale?.dispose()
        super.dismiss()
    }

    override fun show() {
        tagDisposale = viewModel.tagRepository.getTags().subscribe({
            setTags(it)
        }, RxErrorHandler.handleEmptyError())
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
        val leftPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
        val verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
        for (tag in tags) {
            val tagCheckbox = AppCompatCheckBox(context)
            tagCheckbox.text = tag.name
            tagCheckbox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tagCheckbox.isChecked = viewModel.tags.contains(tag.id)
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
                    viewModel.addActiveTag(tag.id)
                } else {
                    viewModel.removeActiveTag(tag.id)
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
            TextViewCompat.setCompoundDrawableTintList(button, ColorStateList.valueOf(context.getThemeColor(R.attr.colorAccent)))
        }
        button.elevation = 0f
        button.setBackgroundResource(R.drawable.button_background_gray_700)
        button.setShadowLayer(0f, 0f, 0f, ContextCompat.getColor(context, R.color.content_background))
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
        val editBinding = EditTagItemBinding.inflate(inflater, binding.tagsList, false)
        editBinding.editText.setText(tag.name)
        editBinding.editText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        editBinding.editText.addTextChangedListener(
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
        editBinding.deleteButton.setOnClickListener {
            deletedTags.add(tag.id)
            if (createdTags.containsKey(tag.id)) {
                createdTags.remove(tag.id)
            }
            if (editedTags.containsKey(tag.id)) {
                editedTags.remove(tag.id)
            }
            viewModel.tags.remove(tag.id)
            tags.remove(tag)
            binding.tagsList.removeView(editBinding.root)
        }
        binding.tagsList.addView(editBinding.root)
    }

    fun setActiveTags(tagIds: MutableList<String>?) {
        if (tagIds == null) {
            this.viewModel.tags.clear()
        } else {
            this.viewModel.tags = tagIds
        }
        for (index in 0 until binding.tagsList.childCount - 1) {
            (binding.tagsList.getChildAt(index) as? AppCompatCheckBox)?.isChecked = false
        }
        for (tagId in this.viewModel.tags) {
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

    private fun setActiveFilter(activeFilter: String?) {
        viewModel.setActiveFilter(taskType, activeFilter ?: Task.FILTER_ALL)
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
        val newFilter = when (checkedId) {
            R.id.second_task_filter -> when (taskType) {
                TaskType.HABIT -> Task.FILTER_WEAK
                TaskType.DAILY -> Task.FILTER_ACTIVE
                TaskType.TODO -> Task.FILTER_DATED
                else -> Task.FILTER_ALL
            }
            R.id.third_task_filter -> when (taskType) {
                TaskType.HABIT -> Task.FILTER_STRONG
                TaskType.DAILY -> Task.FILTER_GRAY
                TaskType.TODO -> Task.FILTER_COMPLETED
                else -> Task.FILTER_ALL
            }
            else -> if (taskType != TaskType.TODO) {
                Task.FILTER_ALL
            } else {
                Task.FILTER_ACTIVE
            }
        }
        viewModel.setActiveFilter(taskType, newFilter)
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
        binding.clearButton.isEnabled = viewModel.isFiltering(taskType)
        binding.clearButton.setTextColor(
            if (binding.clearButton.isEnabled) {
                context.getThemeColor(R.attr.colorAccent)
            } else {
                ContextCompat.getColor(context, R.color.text_dimmed)
            }
        )
    }
}
