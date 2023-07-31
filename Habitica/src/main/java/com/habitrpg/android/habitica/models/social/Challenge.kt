package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.Date

open class Challenge : RealmObject(), BaseMainObject {

    @PrimaryKey
    var id: String? = null
    var name: String? = null
    var shortName: String? = null
    var description: String? = null
    var leaderName: String? = null
    var leaderId: String? = null
    var groupName: String? = null
    var groupId: String? = null
    var prize: Int = 0
    var official: Boolean = false
    var memberCount: Int = 0
    var todoList: String? = null
    var habitList: String? = null
    var dailyList: String? = null
    var rewardList: String? = null
    var createdAt: Date? = null
    var updatedAt: Date? = null

    var group: Group? = null

    var leader: User? = null

    @Ignore
    var tasksOrder: TasksOrder? = null
    var summary: String? = null

    fun getTasksOrder(): HashMap<String, Array<String>> {
        val map = HashMap<String, Array<String>>()

        if (dailyList?.isNotEmpty() == true) {
            dailyList?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.let { map[TASK_ORDER_DAILYS] }
        }

        if (habitList?.isNotEmpty() == true) {
            habitList?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.let { map[TASK_ORDER_HABITS] }
        }

        if (rewardList?.isNotEmpty() == true) {
            rewardList?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.let { map[TASK_ORDER_REWARDS] }
        }

        if (todoList?.isNotEmpty() == true) {
            todoList?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()?.let { map[TASK_ORDER_TODOS] }
        }

        return map
    }

    override val realmClass: Class<out RealmModel>
        get() = Challenge::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == Challenge::class.java && this.id != null) {
            this.id == (other as Challenge).id
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    companion object {

        const val TASK_ORDER_HABITS = "habits"
        const val TASK_ORDER_TODOS = "todos"
        const val TASK_ORDER_DAILYS = "dailys"
        const val TASK_ORDER_REWARDS = "rewards"
    }
}
