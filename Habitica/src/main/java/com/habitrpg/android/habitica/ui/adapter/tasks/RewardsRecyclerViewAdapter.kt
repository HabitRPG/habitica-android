package com.habitrpg.android.habitica.ui.adapter.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.ShopItemViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject

class RewardsRecyclerViewAdapter(
    private var customRewards: List<Task>?,
    private val layoutResource: Int
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
    override var canScoreTasks = true
    private var inAppRewards: List<ShopItem>? = null

    private val errorButtonEventsSubject: PublishSubject<String> = PublishSubject.create()
    override val errorButtonEvents: Flowable<String> = errorButtonEventsSubject.toFlowable(BackpressureStrategy.DROP)
    private var taskScoreEventsSubject: PublishSubject<Pair<Task, TaskDirection>> = PublishSubject.create()
    override val taskScoreEvents: Flowable<Pair<Task, TaskDirection>> = taskScoreEventsSubject.toFlowable(BackpressureStrategy.LATEST)
    private var checklistItemScoreSubject: PublishSubject<Pair<Task, ChecklistItem>> = PublishSubject.create()
    override val checklistItemScoreEvents: Flowable<Pair<Task, ChecklistItem>> = checklistItemScoreSubject.toFlowable(BackpressureStrategy.DROP)
    private var taskOpenEventsSubject: PublishSubject<Pair<Task, View>> = PublishSubject.create()
    override val taskOpenEvents: Flowable<Pair<Task, View>> = taskOpenEventsSubject.toFlowable(BackpressureStrategy.LATEST)
    private var brokenTaskEventsSubject: PublishSubject<Task> = PublishSubject.create()
    override val brokenTaskEvents: Flowable<Task> = brokenTaskEventsSubject.toFlowable(BackpressureStrategy.DROP)
    override val adventureGuideOpenEvents: Flowable<Boolean>? = null
    private var purchaseCardSubject: PublishSubject<ShopItem> = PublishSubject.create()
    val purchaseCardEvents: Flowable<ShopItem> = purchaseCardSubject.toFlowable(BackpressureStrategy.LATEST)

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
                        taskScoreEventsSubject.onNext(Pair(task, direction))
                    }
                },
                { task -> taskOpenEventsSubject.onNext(task) }
            ) { task -> brokenTaskEventsSubject.onNext(task) }
        } else {
            val viewHolder = ShopItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopitem, parent, false))
            viewHolder.purchaseCardAction = { purchaseCardSubject.onNext(it) }
            viewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (customRewards != null && position < customRewardCount) {
            val reward = customRewards?.get(position) ?: return
            val gold = user?.stats?.gp ?: 0.0
            (holder as? RewardViewHolder)?.isLocked = !canScoreTasks
            (holder as? RewardViewHolder)?.bind(reward, position, reward.value <= gold, taskDisplayMode)
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
        return if (customRewards != null && position < customRewardCount) {
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
        rewardCount += inAppRewardCount
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
        private const val VIEWTYPE_IN_APP_REWARD = 2
    }
}
