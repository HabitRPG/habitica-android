package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.OnClick
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.TaskTappedEvent
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.helpers.NumberAbbreviator
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.ItemDetailDialog
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import org.greenrobot.eventbus.EventBus

class RewardViewHolder(itemView: View) : BaseTaskViewHolder(itemView) {

    internal val buyButton: View by bindView(itemView, R.id.buyButton)
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
        val event = BuyRewardCommand()
        event.Reward = task
        EventBus.getDefault().post(event)
    }

    override fun onClick(v: View) {
        if (task?.isValid != true) {
            return
        }
        if (isItem) {
            val dialog = ItemDetailDialog(context)
            dialog.setTitle(task?.text)
            dialog.setDescription(task?.notes)
            dialog.setImage("shop_" + this.task?.id)
            dialog.setCurrency("gold")
            dialog.setValue(task!!.value)
            dialog.setBuyListener { _, _ -> this.buyReward() }
            dialog.show()
        } else {
            val event = TaskTappedEvent()
            event.Task = task

            EventBus.getDefault().post(event)
        }
    }

    override fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)

        this.buyButton.isEnabled = !taskActionsDisabled
    }

    fun bindHolder(reward: Task, position: Int, canBuy: Boolean) {
        this.task = reward
        super.bindHolder(reward, position)
        this.priceLabel.text = NumberAbbreviator.abbreviate(itemView.context, this.task!!.value)

        if (canBuy) {
            goldIconView.alpha = 1.0f
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.yellow_50))
        } else {
            goldIconView.alpha = 0.4f
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
        }
    }
}
