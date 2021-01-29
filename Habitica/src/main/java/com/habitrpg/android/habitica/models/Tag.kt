package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.tasks.Task

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Tag : RealmObject() {

    @PrimaryKey
    var id: String = ""

    var userId: String? = null
    var tasks: RealmList<Task>? = null
    var name: String = ""
    internal var challenge: Boolean = false

    fun getTasks(): List<Task>? {
        return tasks
    }


    override fun equals(other: Any?): Boolean {
        if (other is Tag) {
            return this.id == other.id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
