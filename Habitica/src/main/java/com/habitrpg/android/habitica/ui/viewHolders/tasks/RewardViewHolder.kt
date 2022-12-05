package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toDrawable
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RewardItemCardBinding
import com.habitrpg.android.habitica.helpers.GroupPlanInfoProvider
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.ItemDetailDialog
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.helpers.NumberAbbreviator
import com.habitrpg.shared.habitica.models.responses.TaskDirection

class RewardViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    openTaskFunc: ((Pair<Task, View>) -> Unit),
    brokenTaskFunc: ((Task) -> Unit),
    assignedTextProvider: GroupPlanInfoProvider?
) : BaseTaskViewHolder(
    itemView,
    scoreTaskFunc,
    openTaskFunc,
    brokenTaskFunc,
    assignedTextProvider
) {
    private val binding = RewardItemCardBinding.bind(itemView)

    private val isItem: Boolean
        get() = this.task?.specialTag == "item"

    init {
        binding.buyButton.setOnClickListener {
            buyReward()
        }
        binding.goldIcon.setImageBitmap(HabiticaIconsHelper.imageOfGold())
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
        binding.priceLabel.text =
            NumberAbbreviator.abbreviate(itemView.context, this.task?.value ?: 0.0)

        if (isLocked) {
            binding.priceLabel.setCompoundDrawablesWithIntrinsicBounds(
                HabiticaIconsHelper.imageOfLocked(
                    ContextCompat.getColor(context, R.color.gray_1_30), 10, 12
                ).toDrawable(context.resources), null, null, null
            )
            binding.priceLabel.compoundDrawablePadding = 2.dpToPx(context)
        } else {
            binding.priceLabel.setCompoundDrawables(null, null, null, null)
        }
        if (canBuy && !isLocked) {
            binding.goldIcon.alpha = 1.0f
            binding.priceLabel.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.reward_buy_button_text
                )
            )
            binding.buyButton.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.reward_buy_button_bg
                )
            )
        } else {
            binding.goldIcon.alpha = 0.6f
            binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
            binding.buyButton.setBackgroundColor(
                ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(
                        context,
                        R.color.offset_background
                    ), 127
                )
            )
        }
    }
}
