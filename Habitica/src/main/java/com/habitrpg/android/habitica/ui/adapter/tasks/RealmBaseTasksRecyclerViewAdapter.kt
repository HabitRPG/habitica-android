package com.habitrpg.android.habitica.ui.adapter.tasks

import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.AdventureGuideMenuBannerBinding
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.subjects.PublishSubject
import io.realm.OrderedRealmCollection

abstract class RealmBaseTasksRecyclerViewAdapter(
    private val layoutResource: Int,
    val viewModel: TasksViewModel
) : BaseRecyclerViewAdapter<Task, RecyclerView.ViewHolder>(), TaskRecyclerViewAdapter {
    private var unfilteredData: List<Task>? = null
    override var showAdventureGuide = false
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }
    override var user: User? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override var taskDisplayMode: String = "standard"
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    private var errorButtonEventsSubject: PublishSubject<String> = PublishSubject.create()
    override val errorButtonEvents: Flowable<String> = errorButtonEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var taskScoreEventsSubject: PublishSubject<Pair<Task, TaskDirection>> = PublishSubject.create()
    override val taskScoreEvents: Flowable<Pair<Task, TaskDirection>> = taskScoreEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var checklistItemScoreSubject: PublishSubject<Pair<Task, ChecklistItem>> = PublishSubject.create()
    override val checklistItemScoreEvents: Flowable<Pair<Task, ChecklistItem>> = checklistItemScoreSubject.toFlowable(BackpressureStrategy.DROP)
    protected var taskOpenEventsSubject: PublishSubject<Pair<Task, View>> = PublishSubject.create()
    override val taskOpenEvents: Flowable<Pair<Task, View>> = taskOpenEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var brokenTaskEventsSubject: PublishSubject<Task> = PublishSubject.create()
    override val brokenTaskEvents: Flowable<Task> = brokenTaskEventsSubject.toFlowable(BackpressureStrategy.DROP)
    protected var adventureGuideOpenSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val adventureGuideOpenEvents: Flowable<Boolean> = adventureGuideOpenSubject.toFlowable(BackpressureStrategy.DROP)

    override fun getItemId(index: Int): Long = index.toLong()

    override fun updateUnfilteredData(data: List<Task>?) {
        unfilteredData = data
        this.data = data ?: emptyList()
        filter()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AdventureGuideMenuBannerBinding.inflate(parent.context.layoutInflater, parent, false)
        return AdventureGuideViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null && holder is BaseTaskViewHolder) {
            holder.isLocked = !viewModel.canScoreTask(item)
            holder.bind(item, position, taskDisplayMode, viewModel.ownerID.value)
            holder.errorButtonClicked = Action {
                errorButtonEventsSubject.onNext("")
            }
        } else if (holder is AdventureGuideViewHolder) {
            holder.itemView.setOnClickListener { adventureGuideOpenSubject.onNext(true) }
            user?.let { holder.update(it) }
        }
    }

    override fun getItemCount(): Int {
        return data.size + if (showAdventureGuide) 1 else 0
    }

    override fun getItem(position: Int): Task? {
        if (showAdventureGuide && position == 0) {
            return null
        } else if (showAdventureGuide) {
            return super.getItem(position - 1)
        }
        return super.getItem(position)
    }

    override fun getItemViewType(position: Int): Int {
        if (showAdventureGuide && position == 0) return 1
        return super.getItemViewType(position)
    }

    internal fun getContentView(parent: ViewGroup): View = getContentView(parent, layoutResource)

    private fun getContentView(parent: ViewGroup, layoutResource: Int): View =
        LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)

    final override fun filter() {
        val unfilteredData = this.unfilteredData ?: return

        if (unfilteredData is OrderedRealmCollection) {
            val query = viewModel.createQuery(unfilteredData)
            if (query != null) {
                data = query.findAll()
            }
        } else {
            data = unfilteredData
        }
    }
}

class AdventureGuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
    private var countView: TextView = itemView.findViewById(R.id.count_view)

    init {
        itemView.findViewById<TextView>(R.id.gold_textview).setCompoundDrawablesWithIntrinsicBounds(
            BitmapDrawable(itemView.resources, HabiticaIconsHelper.imageOfGold()),
            null,
            null,
            null
        )
        itemView.findViewById<TextView>(R.id.gold_textview).compoundDrawablePadding = 4.dpToPx(itemView.context)
    }

    fun update(user: User) {
        val achievements = user.onboardingAchievements
        val completed = achievements.count { it.earned }
        progressBar.max = achievements.size
        progressBar.progress = completed
        countView.text = "$completed / ${achievements.size}"
    }
}
