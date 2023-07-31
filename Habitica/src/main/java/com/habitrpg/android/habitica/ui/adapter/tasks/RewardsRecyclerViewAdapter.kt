package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.ShopItemViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.shared.habitica.models.responses.TaskDirection

class RewardsRecyclerViewAdapter(
    private var customRewards: List<Task>?,
    private val layoutResource: Int,
    val viewModel: TasksViewModel
) : BaseRecyclerViewAdapter<Task, RecyclerView.ViewHolder>(), TaskRecyclerViewAdapter {
    override var user: User? = null
        set(value) {
            if (field?.versionNumber == value?.versionNumber) {
                return
            }
            field = value
            notifyDataSetChanged()
        }
    override var showAdventureGuide: Boolean = false
    private var inAppRewards: List<ShopItem>? = null

    override var errorButtonEvents: ((String) -> Unit)? = null
    override var taskScoreEvents: ((Task, TaskDirection) -> Unit)? = null
    override var checklistItemScoreEvents: ((Task, ChecklistItem) -> Unit)? = null
    override var taskOpenEvents: ((Task, View) -> Unit)? = null
    override var brokenTaskEvents: ((Task) -> Unit)? = null
    override var adventureGuideOpenEvents: ((Boolean) -> Unit)? = null
    var purchaseCardEvents: ((ShopItem) -> Unit)? = null
    var onShowPurchaseDialog: ((ShopItem, Boolean) -> Unit)? = null

    override var taskDisplayMode: String = "standard"
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    private val inAppRewardCount: Int
        get() {
            // if (inAppRewards?.isValid != true) return 0
            return inAppRewards?.size ?: 0
        }

    private val customRewardCount: Int
        get() {
            // if (customRewards?.isValid != true) return 0
            return customRewards?.size ?: 0
        }

    private fun getContentView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEWTYPE_CUSTOM_REWARD) {
            RewardViewHolder(
                getContentView(parent),
                { task, direction ->
                    if (task.value <= (user?.stats?.gp ?: 0.0)) {
                        taskScoreEvents?.invoke(task, direction)
                    }
                },
                { task -> taskOpenEvents?.invoke(task.first, task.second) },
                {
                        task ->
                    brokenTaskEvents?.invoke(task)
                },
                viewModel
            )
        } else {
            val viewHolder = ShopItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopitem, parent, false))
            viewHolder.purchaseCardAction = { purchaseCardEvents?.invoke(it) }
            viewHolder.onShowPurchaseDialog = onShowPurchaseDialog
            viewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (customRewards != null && position < customRewardCount) {
            val reward = customRewards?.get(position) ?: return
            val gold = user?.stats?.gp ?: 0.0
            (holder as? RewardViewHolder)?.isLocked = !viewModel.canScoreTask(reward)
            (holder as? RewardViewHolder)?.bind(reward, position, reward.value <= gold, taskDisplayMode, viewModel.ownerID.value)
        } else if (inAppRewards != null) {
            val item = inAppRewards?.get(position - customRewardCount) ?: return
            if (holder is ShopItemViewHolder) {
                holder.bind(item, item.canAfford(user, 1), 0)
                holder.isPinned = true
                holder.hidePinIndicator()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if ((customRewards != null && position < customRewardCount) || (customRewardCount == 0 && inAppRewardCount == 0)) {
            VIEWTYPE_CUSTOM_REWARD
        } else {
            VIEWTYPE_IN_APP_REWARD
        }
    }

    override fun updateUnfilteredData(data: List<Task>?) {
        updateData(data)
    }

    override fun getItemCount(): Int {
        var rewardCount = customRewardCount
        if (viewModel.isPersonalBoard) {
            rewardCount += inAppRewardCount
        }
        return rewardCount
    }

    fun updateData(tasks: List<Task>?) {
        this.customRewards = tasks
        notifyDataSetChanged()
    }

    fun updateItemRewards(items: List<ShopItem>) {
        if (items.isNotEmpty()) {
            if (Task::class.java.isAssignableFrom(items.first().javaClass)) {
                // this catches a weird bug where the observable gets a list of tasks for no apparent reason.
                return
            }
        }
        this.inAppRewards = items
        notifyDataSetChanged()
    }

    override fun filter() { /* no-on */ }

    companion object {
        private const val VIEWTYPE_CUSTOM_REWARD = 0
        private const val VIEWTYPE_IN_APP_REWARD = 3
    }
}
