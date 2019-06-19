package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.content.DialogInterface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.NumberAbbreviator
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.ItemDetailDialog
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class RewardViewHolder(itemView: View, scoreTaskFunc: ((Task, TaskDirection) -> Unit), openTaskFunc: ((Task) -> Unit)) : BaseTaskViewHolder(itemView, scoreTaskFunc, openTaskFunc) {

    private val buyButton: View by bindView(itemView, R.id.buyButton)
    internal val priceLabel: TextView by bindView(itemView, R.id.priceLabel)
    private val goldIconView: ImageView by bindView(itemView, R.id.gold_icon)


    private val isItem: Boolean
        get() = this.task?.specialTag == "item"

    init {
        goldIconView.setImageBitmap(HabiticaIconsHelper.imageOfGold())

        buyButton.setOnClickListener {
            buyReward()
        }
    }

    override fun canContainMarkdown(): Boolean {
        return !isItem
    }

    private fun buyReward() {
        task?.let { scoreTaskFunc(it, TaskDirection.UP) }
    }

    override fun onClick(v: View) {
        if (task?.isValid != true) {
            return
        }
        if (isItem) {
            val dialog = ItemDetailDialog(context)
            dialog.setTitle(task?.text)
            dialog.setDescription(task?.notes ?: "")
            dialog.setImage("shop_" + this.task?.id)
            dialog.setCurrency("gold")
            dialog.setValue(task?.value)
            dialog.setBuyListener( DialogInterface.OnClickListener { _, _ -> this.buyReward() })
            dialog.show()
        } else {
            super.onClick(v)
        }
    }

    override fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)

        this.buyButton.isEnabled = !taskActionsDisabled
    }

    fun bind(reward: Task, position: Int, canBuy: Boolean) {
        this.task = reward
        super.bind(reward, position)
        this.priceLabel.text = NumberAbbreviator.abbreviate(itemView.context, this.task?.value ?: 0.0)

        if (canBuy) {
            goldIconView.alpha = 1.0f
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.yellow_5))
        } else {
            goldIconView.alpha = 0.4f
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
        }
    }
}
