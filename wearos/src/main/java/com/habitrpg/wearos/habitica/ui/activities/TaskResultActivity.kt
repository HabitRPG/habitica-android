package com.habitrpg.wearos.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.widget.GridLayout
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTaskResultBinding
import com.habitrpg.android.habitica.databinding.TaskRewardDropBinding
import com.habitrpg.android.habitica.extensions.localizedCapitalize
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskResultViewModel
import com.habitrpg.wearos.habitica.ui.views.TaskRewardChip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class TaskResultActivity : BaseActivity<ActivityTaskResultBinding, TaskResultViewModel>() {
    override val viewModel: TaskResultViewModel by viewModels()

    private var secondsToShow = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskResultBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        makeChips()

        binding.root.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            delay(secondsToShow.toDuration(DurationUnit.SECONDS))
            finish()
        }
    }

    override fun finish() {
        if (viewModel.result?.hasLeveledUp == true) {
            startActivity(Intent(this, LevelupActivity::class.java))
        }
        super.finish()
    }

    private fun makeChips() {
        binding.gridLayout.removeAllViews()
        var chips = mutableListOf<TaskRewardChip>()
        if ((viewModel.result?.healthDelta ?: 0.0) != 0.0) {
            val chip = TaskRewardChip(this)
            chip.set(
                viewModel.result?.healthDelta,
                R.drawable.heart
            )
            chips.add(chip)
        }
        if ((viewModel.result?.experienceDelta ?: 0.0) != 0.0) {
            val chip = TaskRewardChip(this)
            chip.set(
                viewModel.result?.experienceDelta,
                R.drawable.experience
            )
            chips.add(chip)
        }
        if ((viewModel.result?.goldDelta ?: 0.0) != 0.0) {
            val chip = TaskRewardChip(this)
            chip.set(
                viewModel.result?.goldDelta,
                R.drawable.gold
            )
            chips.add(chip)
        }
        if ((viewModel.result?.manaDelta ?: 0.0) != 0.0) {
            val chip = TaskRewardChip(this)
            chip.set(
                viewModel.result?.manaDelta,
                R.drawable.magic
            )
            chips.add(chip)
        }
        /*if ((viewModel.result?.questDamage ?: 0.0) > 0.0) {
            val chip = TaskRewardChip(this)
            chip.set(
                viewModel.result?.questDamage,
                HabiticaIconsHelper.imageOfDamage()
            )
            chips.add(chip)
        }*/
        var index = 0
        var currentRow = 0
        var currentColumn = 0
        val hasDrop = viewModel.hasDrop
        val margin = 6.dpToPx(this)
        val chipSize = when {
            hasDrop -> TaskRewardChip.Size.SMALL
            chips.size <= 2 -> TaskRewardChip.Size.LARGE
            chips.size == 5 -> TaskRewardChip.Size.SMALL
            else -> TaskRewardChip.Size.MEDIUM
        }
        if (chips.size > 4 && hasDrop || (chips.size > 5 && !hasDrop)) {
            chips = chips.subList(0, if (hasDrop) 4 else 5)
        }
        secondsToShow = chips.size
        chips.forEach {
            binding.gridLayout.addView(it)
            it.size = chipSize
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
            layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT
            layoutParams.setGravity(Gravity.CENTER)
            layoutParams.rowSpec = GridLayout.spec(currentRow)
            if (index == 0 || currentRow == 2) {
                layoutParams.columnSpec = GridLayout.spec(0, 3, GridLayout.CENTER)
                currentRow += 1
            } else {
                layoutParams.columnSpec = GridLayout.spec(currentColumn, 1)
                if (currentColumn > 0) {
                    layoutParams.marginStart = margin
                }
                currentColumn += 1
                if ((index == 2 && !hasDrop && chips.size == 4) || (index == 3 && !hasDrop && chips.size == 5)) {
                    currentRow += 1
                    currentColumn = 0
                }
            }
            layoutParams.bottomMargin = margin
            index += 1
            it.layoutParams = layoutParams

            val animator = AlphaAnimation(0f, 1f)
            animator.startOffset = (index * 150).toLong() + 200
            animator.duration = 300
            animator.fillAfter = true
            animator.fillBefore = true
            it.startAnimation(animator)
        }
        if (hasDrop) {
            val dropBinding = TaskRewardDropBinding.inflate(layoutInflater, binding.gridLayout, true)
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT
            layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT
            layoutParams.setGravity(Gravity.CENTER)
            layoutParams.rowSpec = GridLayout.spec(2)
            layoutParams.columnSpec = GridLayout.spec(0, 3, GridLayout.CENTER)
            dropBinding.root.layoutParams = layoutParams
            val elements = mutableListOf<String>()
            /* if ((viewModel.result?.questItemsFound?: 0) != 0) {
                if (viewModel.result?.questItemsFound == 1) {
                    elements.add(getString(R.string.one_quest_item))
                } else {
                    elements.add(getString(R.string.x_quest_item, viewModel.result?.questItemsFound))
                }
            }*/
            if (viewModel.result?.drop?.key != null) {
                val type = viewModel.result?.drop?.type
                val key = viewModel.result?.drop?.key
                elements.add(when (type) {
                    "Food" -> getString(R.string.some_food)
                    "Egg" -> getString(R.string.an_egg)
                    "HatchingPotion" -> getString(R.string.a_potion)
                    else -> getString(R.string.some_x, type)
                })
                dropBinding.imageView.loadImage("Pet_" + type + "_" + key)
            }
            dropBinding.textView.text = when (elements.size) {
                1 -> elements[0]
                2 -> getString(R.string.x_and_y, elements[0], elements[1])
                else -> elements.joinToString(", ")
            }.localizedCapitalize()
        }
    }

    companion object {
        fun show(context: Activity, result: TaskScoringResult) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (sharedPreferences.getBoolean("hide_task_results", false)) return
            val intent = Intent(context, TaskResultActivity::class.java)
            intent.putExtra("result", result)
            context.startActivity(intent)
        }
    }
}