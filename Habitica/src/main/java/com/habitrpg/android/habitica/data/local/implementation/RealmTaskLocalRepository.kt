package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder

import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort
import rx.Observable

class RealmTaskLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TaskLocalRepository {

    override fun getTasks(taskType: String, userID: String): Observable<RealmResults<Task>> {
        return realm.where(Task::class.java)
                .equalTo("type", taskType)
                .equalTo("userId", userID)
                .findAllSorted("position")
                .sort("dateCreated", Sort.DESCENDING)
                .asObservable()
                .filter({ it.isLoaded })
                .retry(1)
    }

    override fun getTasks(userId: String): Observable<RealmResults<Task>> {
        return realm.where(Task::class.java).equalTo("userId", userId).findAll().asObservable()
                .filter({ it.isLoaded })
    }

    override fun saveTasks(userId: String, tasksOrder: TasksOrder, tasks: TaskList) {
        val sortedTasks = ArrayList<Task>()
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.habits))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.dailys))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.todos))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.rewards))
        removeOldTasks(userId, sortedTasks)

        val allChecklistItems = ArrayList<ChecklistItem>()
        for (t in sortedTasks) {
            allChecklistItems.addAll(t.checklist)
        }
        removeOldChecklists(allChecklistItems)

        val allReminders = ArrayList<RemindersItem>()
        for (t in sortedTasks) {
            allReminders.addAll(t.reminders)
        }
        removeOldReminders(allReminders)
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(sortedTasks) }
    }

    private fun sortTasks(taskMap: MutableMap<String, Task>, taskOrder: List<String>): List<Task> {
        val taskList = ArrayList<Task>()
        var position = 0
        for (taskId in taskOrder) {
            val task = taskMap[taskId]
            if (task != null) {
                task.position = position
                taskList.add(task)
                position++
                taskMap.remove(taskId)
            }
        }
        return taskList
    }

    override fun saveTask(task: Task) {
        realm.executeTransaction { realm1 -> realm1.insertOrUpdate(task) }
    }

    private fun removeOldTasks(userID: String, onlineTaskList: List<Task>) {
        val localTasks = realm.where(Task::class.java).equalTo("userId", userID).findAll().createSnapshot()
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        realm.executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.checklist.deleteAllFromRealm()
                localTask.reminders.deleteAllFromRealm()
                localTask.deleteFromRealm()
            }
        }
    }

    private fun removeOldChecklists(onlineItems: List<ChecklistItem>) {
        val localItems = realm.where(ChecklistItem::class.java).findAll().createSnapshot()
        val itemsToDelete = localItems.filterNot { onlineItems.contains(it) }
        realm.executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    private fun removeOldReminders(onlineReminders: List<RemindersItem>) {
        val localReminders = realm.where(RemindersItem::class.java).findAll().createSnapshot()
        val itemsToDelete = localReminders.filterNot { onlineReminders.contains(it) }
        realm.executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    override fun deleteTask(taskID: String) {
        val task = realm.where(Task::class.java).equalTo("id", taskID).findFirstAsync()
        realm.executeTransaction { task.deleteFromRealm() }
    }

    override fun getTask(taskId: String): Observable<Task> {
        return realm.where(Task::class.java).equalTo("id", taskId).findFirstAsync().asObservable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Task::class.java)
    }

    override fun getTaskCopy(taskId: String): Observable<Task> {
        return getTask(taskId)
                .map { task ->
                    if (task.isManaged && task.isValid) {
                        return@map realm.copyFromRealm<Task>(task)
                    } else {
                        return@map task
                    }
                }
    }

    override fun markTaskCompleted(taskId: String, isCompleted: Boolean) {
        val task = realm.where(Task::class.java).equalTo("id", taskId).findFirstAsync()
        realm.executeTransaction { task.completed = true }
    }

    override fun saveReminder(remindersItem: RemindersItem) {
        realm.executeTransaction { it.insertOrUpdate(remindersItem) }
    }

    override fun swapTaskPosition(firstPosition: Int, secondPosition: Int) {
        val firstTask = realm.where(Task::class.java).equalTo("position", firstPosition).findFirst()
        val secondTask = realm.where(Task::class.java).equalTo("position", secondPosition).findFirst()
        if (firstTask != null && secondTask != null && firstTask.isValid && secondTask.isValid) {
            realm.executeTransaction {
                firstTask.position = secondPosition
                secondTask.position = firstPosition
            }
        }
    }

    override fun getTaskAtPosition(currentPosition: Int): Observable<Task> {
        return realm.where(Task::class.java).equalTo("position", currentPosition).findFirstAsync().asObservable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Task::class.java)
    }

    override fun updateIsdue(daily: TaskList): Observable<TaskList> {
        return Observable.just(realm.where(Task::class.java).equalTo("type", "daily").findAll())
                .first()
                .map { tasks ->
                    realm.beginTransaction()
                    tasks.filter { daily.tasks.containsKey(it.id) }.forEach { it.isDue = daily.tasks[it.id]?.isDue }
                    realm.commitTransaction()
                    daily
                }
    }

    override fun updateTaskPositions(taskOrder: List<String>) {
        if (taskOrder.isNotEmpty()) {
            val tasks = realm.where(Task::class.java).`in`("id", taskOrder.toTypedArray()).findAll()
            realm.executeTransaction {
                tasks.filter { taskOrder.contains(it.id) }.forEach { it.position = taskOrder.indexOf(it.id) }
            }
        }
    }
}
