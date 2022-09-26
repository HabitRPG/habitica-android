package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RewardItemCardBinding
import com.habitrpg.android.habitica.helpers.AssignedTextProvider
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.ItemDetailDialog
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.helpers.NumberAbbreviator
import com.habitrpg.shared.habitica.models.responses.TaskDirection

class RewardViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    openTaskFunc: ((Pair<Task, View>) -> Unit),
    brokenTaskFunc: ((Task) -> Unit),
    assignedTextProvider: AssignedTextProvider?
) : BaseTaskViewHolder(itemView, scoreTaskFunc, openTaskFunc, brokenTaskFunc, assignedTextProvider) {
    private val binding = RewardItemCardBinding.bind(itemView)

    private val isItem: Boolean
        get() = this.task?.specialTag == "item"

    init {
        binding.buyButton.setOnClickListener {
            buyReward()
        }
    }

    override fun canContainMarkdown(): Boolean {
        return !isItem
    }

    private fun buyReward() {
        task?.let { scoreTaskFunc(it, TaskDirection.DOWN) }
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (task?.isValid != true) {
            return true
        }
        if (isItem) {
            val dialog = ItemDetailDialog(context)
            dialog.setTitle(task?.text)
            dialog.setDescription(task?.notes ?: "")
            dialog.setImage("shop_" + this.task?.id)
            dialog.setCurrency("gold")
            dialog.setValue(task?.value)
            dialog.setBuyListener { _, _ -> this.buyReward() }
            dialog.show()
        } else {
            super.onTouch(view, motionEvent)
        }
        return true
    }

    override fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)
        binding.buyButton.isEnabled = !taskActionsDisabled
    }

    fun bind(reward: Task, position: Int, canBuy: Boolean, displayMode: String, ownerID: String?) {
        this.task = reward
        streakTextView.visibility = View.GONE
        super.bind(reward, position, displayMode, ownerID)
        binding.priceLabel.text = NumberAbbreviator.abbreviate(itemView.context, this.task?.value ?: 0.0)

        if (isLocked) {
            binding.goldIcon.setImageResource(R.drawable.task_lock)
            binding.goldIcon.drawable.setTint(ContextCompat.getColor(context, R.color.reward_buy_button_text))
            binding.goldIcon.alpha = 1.0f
            binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.reward_buy_button_text))
            binding.buyButton.setBackgroundColor(ContextCompat.getColor(context, R.color.reward_buy_button_bg))
        } else {
            binding.goldIcon.setImageBitmap(HabiticaIconsHelper.imageOfGold())
            if (canBuy) {
                binding.goldIcon.alpha = 1.0f
                binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.reward_buy_button_text))
                binding.buyButton.setBackgroundColor(ContextCompat.getColor(context, R.color.reward_buy_button_bg))
            } else {
                binding.goldIcon.alpha = 0.6f
                binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
                binding.buyButton.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(context, R.color.offset_background), 127))
            }
        }
    }
}
