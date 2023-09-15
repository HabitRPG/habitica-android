package com.habitrpg.android.habitica.ui.views.tasks

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.databinding.DialogTaskFilterBinding
import com.habitrpg.android.habitica.databinding.EditTagItemBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.util.UUID

class TaskFilterDialog(context: Context, private val repository: TagRepository, private val showTags: Boolean) : HabiticaBottomSheetDialog(context) {
    lateinit var viewModel: TasksViewModel
    private val binding = DialogTaskFilterBinding.inflate(layoutInflater)

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
    private var isEditingTags: Boolean = false

    init {
        addIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_purple_300_36dp)

        setTitle(R.string.filters)
        this.setContentView(binding.root)

        // Need to use this instead of RadioGroup.onCheckedChangeListener, because that fires twice per change
        binding.allTaskFilter.setOnClickListener {
            onCheckedChanged(binding.taskFilterWrapper, binding.taskFilterWrapper.checkedRadioButtonId)
        }
        binding.secondTaskFilter.setOnClickListener {
            onCheckedChanged(binding.taskFilterWrapper, binding.taskFilterWrapper.checkedRadioButtonId)
        }
        binding.thirdTaskFilter.setOnClickListener {
            onCheckedChanged(binding.taskFilterWrapper, binding.taskFilterWrapper.checkedRadioButtonId)
        }

        binding.clearButton.setOnClickListener {
            if (isEditingTags) {
                stopEditing()
            }
            setActiveFilter(null)
            setActiveTags(null)
        }

        if (showTags) {
            binding.tagEditButton.setOnClickListener { editButtonClicked() }
        } else {
            binding.tagsList.isVisible = false
            binding.tagsTitle.isVisible = false
            binding.tagEditButton.isVisible = false
        }
    }

    override fun show() {
        if (showTags) {
            lifecycleScope.launchCatching {
                viewModel.tagRepository.getTags().collect {
                    setTags(it)
                }
            }
        }
        super.show()
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    private fun setTags(tags: List<Tag>) {
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
        sortTagPositions()
        for (tag in tags) {
            if (tag.id.isBlank()) {
                // Title for tag group
                val view = TextView(context)
                view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
                view.text = tag.name
                view.setTextColor(context.getThemeColor(R.attr.textColorTintedPrimary))
                binding.tagsList.addView(view)
            } else {
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
        }
        createAddTagButton()
    }

    private fun createAddTagButton() {
        val button = MaterialButton(context)
        button.setText(R.string.add_tag)
        button.icon = addIcon
        button.iconTint = ColorStateList.valueOf(context.getThemeColor(R.attr.colorAccent))
        button.iconGravity = MaterialButton.ICON_GRAVITY_START
        button.elevation = 0f
        button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray700_gray10))
        button.setStrokeColorResource(R.color.content_background)
        button.strokeWidth = 0
        button.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        button.setOnClickListener { createTag() }

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
        isEditingTags = true
        binding.tagsList.removeAllViews()
        createTagEditViews()
        binding.tagEditButton.setText(R.string.done)
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private fun stopEditing() {
        isEditingTags = false
        binding.tagsList.removeAllViews()
        createTagViews()
        binding.tagEditButton.setText(R.string.edit_tag_btn_edit)
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        lifecycleScope.launchCatching {
            repository.updateTags(editedTags.values).forEach { editedTags.remove(it.id) }
            repository.createTags(createdTags.values).forEach { tag -> createdTags.remove(tag.id) }
            repeat(repository.deleteTags(deletedTags).size) { deletedTags.clear() }
        }
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

    private fun setActiveTags(tagIds: MutableList<String>?) {
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

    private fun onCheckedChanged(group: RadioGroup, @IdRes checkedId: Int) {
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

    private fun sortTagPositions() {
        val sortedTagList = arrayListOf<Tag>()
        val challengeTagList = arrayListOf<Tag>()
        val groupTagList = arrayListOf<Tag>()
        val otherTagList = arrayListOf<Tag>()

        val challengesTagTitleName = context.getString(R.string.challenge_tags)
        val groupsTagTitleName = context.getString(R.string.group_tags)
        val otherTagTitleName = context.getString(R.string.your_tags)

        tags.forEach {
            if (it.name == challengesTagTitleName || it.name == groupsTagTitleName || it.name == otherTagTitleName) {
                // This tag is a title, skip it.
                return@forEach
            }
            if (it.challenge) {
                challengeTagList.add(it)
            } else if (it.group != null) {
                groupTagList.add(it)
            } else {
                otherTagList.add(it)
            }
        }

        val challengesTagTitle = Tag().apply { name = challengesTagTitleName }
        val groupsTagTitle = Tag().apply { name = groupsTagTitleName }
        val otherTagTitle = Tag().apply { name = otherTagTitleName }

        if (challengeTagList.isNotEmpty() && sortedTagList.none { it.name == challengesTagTitleName }) {
            sortedTagList.add(challengesTagTitle)
            sortedTagList.addAll(challengeTagList)
        }
        if (groupTagList.isNotEmpty() && sortedTagList.none { it.name == groupsTagTitleName }) {
            sortedTagList.add(groupsTagTitle)
            sortedTagList.addAll(groupTagList)
        }
        if (otherTagList.isNotEmpty() && sortedTagList.none { it.name == otherTagTitleName }) {
            sortedTagList.add(otherTagTitle)
            sortedTagList.addAll(otherTagList)
        }

        tags = sortedTagList
    }


    private fun editButtonClicked() {
        isEditingTags = !isEditingTags
        if (isEditingTags) {
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
