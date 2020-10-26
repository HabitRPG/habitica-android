package com.habitrpg.android.habitica.helpers

import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.TaskFilter
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.RealmQuery
import io.realm.Sort
import java.util.*

class TaskFilterHelper {
    var searchQuery: String? = null
    private var tagsId: MutableList<String> = ArrayList()
    private val activeFilters = HashMap<String, String>()

    var tags: MutableList<String>
        get() = this.tagsId
        set(tagsId) {
            this.tagsId = tagsId
        }

    fun howMany(type: String?): Int {
        return this.tagsId.size + if (isTaskFilterActive(type)) 1 else 0
    }

    private fun isTaskFilterActive(type: String?): Boolean {
        if (activeFilters[type] == null) {
            return false
        }
        return if (TaskType.TYPE_TODO == type) {
            TaskFilter.FILTER_ACTIVE != activeFilters[type]
        } else {
            TaskFilter.FILTER_ALL != activeFilters[type]
        }
    }

    fun filter(tasks: List<Task>): List<Task> {
        if (tasks.isEmpty()) {
            return tasks
        }
        val filtered = ArrayList<Task>()
        var activeFilter: String? = null
        if (activeFilters.size > 0) {
            activeFilter = activeFilters[tasks[0].type]
        }
        for (task in tasks) {
            if (isFiltered(task, activeFilter)) {
                filtered.add(task)
            }
        }

        return filtered
    }

    private fun isFiltered(task: Task, activeFilter: String?): Boolean {
        if (!task.containsAllTagIds(tagsId)) {
            return false
        }
        return if (activeFilter != null && activeFilter != TaskFilter.FILTER_ALL) {
            when (activeFilter) {
                TaskFilter.FILTER_ACTIVE -> if (task.type == TaskType.TYPE_DAILY) {
                    task.isDisplayedActive
                } else {
                    !task.completed
                }
                TaskFilter.FILTER_GRAY -> task.completed || !task.isDisplayedActive
                TaskFilter.FILTER_WEAK -> task.value < 1
                TaskFilter.FILTER_STRONG -> task.value >= 1
                TaskFilter.FILTER_DATED -> task.dueDate != null
                TaskFilter.FILTER_COMPLETED -> task.completed
                else -> true
            }
        } else {
            true
        }
    }

    fun setActiveFilter(type: String, activeFilter: String) {
        activeFilters[type] = activeFilter
    }

    fun getActiveFilter(type: String): String? {
        return activeFilters[type]
    }

    fun createQuery(unfilteredData: OrderedRealmCollection<Task>): RealmQuery<Task>? {
        if (!unfilteredData.isValid) {
            return null
        }
        var query = unfilteredData.where()

        if (unfilteredData.size != 0) {
            val taskType = unfilteredData[0].type
            val activeFilter = getActiveFilter(taskType)

            if (tagsId.size > 0) {
                query = query.`in`("tags.id", tagsId.toTypedArray())
            }
            if (searchQuery?.isNotEmpty() == true) {
                query = query
                        .beginGroup()
                        .contains("text", searchQuery ?: "", Case.INSENSITIVE)
                        .or()
                        .contains("notes", searchQuery ?: "", Case.INSENSITIVE)
                        .endGroup()
            }
            if (activeFilter != null && activeFilter != TaskFilter.FILTER_ALL) {
                when (activeFilter) {
                    TaskFilter.FILTER_ACTIVE -> query = if (TaskType.TYPE_DAILY == taskType) {
                        query.equalTo("completed", false).equalTo("isDue", true)
                    } else {
                        query.equalTo("completed", false)
                    }
                    TaskFilter.FILTER_GRAY -> query = query.equalTo("completed", true).or().equalTo("isDue", false)
                    TaskFilter.FILTER_WEAK -> query = query.lessThan("value", 1.0)
                    TaskFilter.FILTER_STRONG -> query = query.greaterThanOrEqualTo("value", 1.0)
                    TaskFilter.FILTER_DATED -> query = query.isNotNull("dueDate").equalTo("completed", false).sort("dueDate")
                    TaskFilter.FILTER_COMPLETED -> query = query.equalTo("completed", true)
                }
            }
            if (activeFilter != TaskFilter.FILTER_DATED) {
                query = query.sort("position", Sort.ASCENDING, "dateCreated", Sort.DESCENDING)
            }
        }
        return query
    }
}
