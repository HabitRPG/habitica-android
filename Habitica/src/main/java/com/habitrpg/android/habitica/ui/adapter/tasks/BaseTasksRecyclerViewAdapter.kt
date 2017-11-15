package com.habitrpg.android.habitica.ui.adapter.tasks

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskFilterHelper
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder
import io.realm.RealmResults

import java.util.ArrayList

import javax.inject.Inject

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.functions.Func1
import rx.schedulers.Schedulers

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
        injectThis(HabiticaBaseApplication.getComponent())

        if (loadFromDatabase()) {
            this.loadContent(true)
        }
    }

    protected abstract fun injectThis(component: AppComponent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredContent!![position]

        holder.bindHolder(item, position)

        /*if (this.displayedChecklist != null && ChecklistedViewHolder.class.isAssignableFrom(holder.getClass())) {
            ChecklistedViewHolder checklistedHolder = (ChecklistedViewHolder) holder;
            checklistedHolder.setDisplayChecklist(this.displayedChecklist == position);
        }*/
    }

    override fun getItemId(position: Int): Long {
        val task = filteredContent!![position]
        return task.id!!.hashCode().toLong()
    }

    override fun getItemCount(): Int = if (filteredContent != null) filteredContent!!.size else 0

    internal fun getContentView(parent: ViewGroup): View = getContentView(parent, layoutResource)

    protected fun getContentView(parent: ViewGroup, layoutResource: Int): View =
            LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)

    fun updateTask(task: Task) {
        if (taskType != task.type)
            return
        var i: Int = 0
        while (i < this.content!!.size) {
            if (content!![i].id == task.id) {
                break
            }
            ++i
        }
        if (i < content!!.size) {
            content!![i] = task
        }
        filter()
    }

    fun filter() {
        if (this.taskFilterHelper == null || this.taskFilterHelper.howMany(taskType) == 0) {
            filteredContent = content
        } else {
            filteredContent = ArrayList()
            filteredContent!!.addAll(this.taskFilterHelper.filter(content))
        }

        this.notifyDataSetChanged()
    }

    fun loadContent(forced: Boolean) {
        if (this.content == null || forced) {
            taskRepository!!.getTasks(this.taskType, this.userID!!)
                    .flatMap<Task>({ Observable.from(it) })
                    .map { task ->
                        task.parseMarkdown()
                        task
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toList()
                    .subscribe(Action1 { this.setTasks(it) }, RxErrorHandler.handleEmptyError())
        }
    }

    fun setTasks(tasks: List<Task>) {
        this.content = ArrayList()
        this.content!!.addAll(tasks)
        filter()
    }

    open fun loadFromDatabase(): Boolean = true

    fun checkTask(task: Task, completed: Boolean?) {
        if (taskType != task.type)
            return

        if (completed!! && task.type == "todo") {
            // remove from the list
            content!!.remove(task)
        }
        this.updateTask(task)
        filter()
    }

    fun addTask(task: Task) {
        if (taskType != task.type)
            return

        content!!.add(0, task)
        filter()
    }

    fun removeTask(deletedTaskId: String) {
        val taskToDelete: Task? = content?.firstOrNull { it.id == deletedTaskId }

        if (taskToDelete != null) {
            content?.remove(taskToDelete)
            filter()
        }
    }

    open fun setDailyResetOffset(dayStart: Int) {

    }
}
