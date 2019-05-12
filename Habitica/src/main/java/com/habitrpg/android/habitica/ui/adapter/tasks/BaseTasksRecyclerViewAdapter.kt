package com.habitrpg.android.habitica.ui.adapter.tasks

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

abstract class BaseTasksRecyclerViewAdapter<VH : BaseTaskViewHolder>(var taskType: String, private val taskFilterHelper: TaskFilterHelper?, private val layoutResource: Int,
                                                                     newContext: Context, private val userID: String?) : RecyclerView.Adapter<VH>() {
    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var taskRepository: TaskRepository
    protected var content: MutableList<Task>? = null
    protected var filteredContent: MutableList<Task>? = null
    internal var context: Context

    init {
        this.setHasStableIds(true)
        this.context = newContext.applicationContext
        this.filteredContent = ArrayList()
        HabiticaBaseApplication.component.notNull { injectThis(it) }

        if (loadFromDatabase()) {
            this.loadContent(true)
        }
    }

    protected abstract fun injectThis(component: AppComponent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredContent?.get(position)
        if (item != null) {
            holder.bindHolder(item, position)
        }
        /*if (this.displayedChecklist != null && ChecklistedViewHolder.class.isAssignableFrom(holder.getClass())) {
            ChecklistedViewHolder checklistedHolder = (ChecklistedViewHolder) holder;
            checklistedHolder.setDisplayChecklist(this.displayedChecklist == position);
        }*/
    }

    override fun getItemId(position: Int): Long {
        val task = filteredContent?.get(position)
        return task?.id?.hashCode()?.toLong() ?: 0
    }

    override fun getItemCount(): Int =filteredContent?.size ?: 0

    internal fun getContentView(parent: ViewGroup): View = getContentView(parent, layoutResource)

    protected fun getContentView(parent: ViewGroup, layoutResource: Int): View =
            LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)

    private fun updateTask(task: Task) {
        if (taskType != task.type)
            return
        var i = 0
        while (i < this.content?.size ?: 0) {
            if (content?.get(i)?.id == task.id) {
                break
            }
            ++i
        }
        if (i < content?.size ?: 0) {
            content?.set(i, task)
        }
        filter()
    }

    fun filter() {
        if (this.taskFilterHelper == null || this.taskFilterHelper.howMany(taskType) == 0) {
            filteredContent = content
        } else {
            filteredContent = ArrayList()
            content.notNull {
                filteredContent?.addAll(this.taskFilterHelper.filter(it))
            }
        }

        this.notifyDataSetChanged()
    }

    private fun loadContent(forced: Boolean) {
        if (this.content == null || forced) {
            taskRepository.getTasks(this.taskType, this.userID ?: "")
                    .flatMap<Task> { Flowable.fromIterable(it) }
                    .map { task ->
                        task.parseMarkdown()
                        task
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toList()
                    .subscribe(Consumer { this.setTasks(it) }, RxErrorHandler.handleEmptyError())
        }
    }

    fun setTasks(tasks: List<Task>) {
        this.content = ArrayList()
        this.content?.addAll(tasks)
        filter()
    }

    open fun loadFromDatabase(): Boolean = true
}
