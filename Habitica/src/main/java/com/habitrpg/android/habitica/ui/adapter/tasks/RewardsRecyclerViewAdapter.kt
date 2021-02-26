package com.habitrpg.android.habitica.ui.adapter.tasks


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.responses.TaskDirection
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
import io.realm.OrderedRealmCollection

class RewardsRecyclerViewAdapter(private var customRewards: OrderedRealmCollection<Task>?, private val layoutResource: Int) : BaseRecyclerViewAdapter<Task, RecyclerView.ViewHolder>(), TaskRecyclerViewAdapter {
    var user: User? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override var canScoreTasks = true
    private var inAppRewards: OrderedRealmCollection<ShopItem>? = null

    private val errorButtonEventsSubject = PublishSubject.create<String>()
    override val errorButtonEvents: Flowable<String> = errorButtonEventsSubject.toFlowable(BackpressureStrategy.DROP)
    private var taskScoreEventsSubject = PublishSubject.create<Pair<Task, TaskDirection>>()
    override val taskScoreEvents: Flowable<Pair<Task, TaskDirection>> = taskScoreEventsSubject.toFlowable(BackpressureStrategy.LATEST)
    private var checklistItemScoreSubject = PublishSubject.create<Pair<Task, ChecklistItem>>()
    override val checklistItemScoreEvents: Flowable<Pair<Task, ChecklistItem>> = checklistItemScoreSubject.toFlowable(BackpressureStrategy.DROP)
    private var taskOpenEventsSubject = PublishSubject.create<Task>()
    override val taskOpenEvents: Flowable<Task> = taskOpenEventsSubject.toFlowable(BackpressureStrategy.LATEST)
    protected var brokenTaskEventsSubject = PublishSubject.create<Task>()
    override val brokenTaskEvents: Flowable<Task> = brokenTaskEventsSubject.toFlowable(BackpressureStrategy.DROP)
    private var purchaseCardSubject = PublishSubject.create<ShopItem>()
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
            if (inAppRewards?.isValid != true) return 0
            return inAppRewards?.size ?: 0
        }

    private val customRewardCount: Int
        get() {
            if (customRewards?.isValid != true) return 0
            return customRewards?.size ?: 0
        }

    override var ignoreUpdates: Boolean
        get() = false
        set(_) {}

    private fun getContentView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEWTYPE_CUSTOM_REWARD) {
            RewardViewHolder(getContentView(parent), { task, direction -> taskScoreEventsSubject.onNext(Pair(task, direction)) }, {
                task -> taskOpenEventsSubject.onNext(task)
            }) {
                task -> brokenTaskEventsSubject.onNext(task)
            }
        } else {
            val viewHolder = ShopItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_shopitem, parent, false))
            viewHolder.purchaseCardAction = {
                purchaseCardSubject.onNext(it)
            }
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
                holder.bind(item, item.canAfford(user, 1))
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

    override fun updateUnfilteredData(data: OrderedRealmCollection<Task>?) {
        updateData(data)
    }

    override fun getItemCount(): Int {
        var rewardCount = customRewardCount
        rewardCount += inAppRewardCount
        return rewardCount
    }

    fun updateData(tasks: OrderedRealmCollection<Task>?) {
        this.customRewards = tasks
        notifyDataSetChanged()
    }

    fun updateItemRewards(items: OrderedRealmCollection<ShopItem>) {
        if (items.size > 0) {
            if (Task::class.java.isAssignableFrom(items.first()!!.javaClass)) {
                //this catches a weird bug where the observable gets a list of tasks for no apparent reason.
                return
            }
        }
        this.inAppRewards = items
        notifyDataSetChanged()
    }

    override fun filter() { /* no-on */ }

    override fun getTaskIDAt(position: Int): String? {
        return customRewards?.get(position)?.id
    }

    companion object {
        private const val VIEWTYPE_CUSTOM_REWARD = 0
        private const val VIEWTYPE_IN_APP_REWARD = 2
    }
}
