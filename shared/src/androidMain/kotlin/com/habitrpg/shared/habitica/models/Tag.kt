package com.habitrpg.shared.habitica.models

import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.Task

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


    override fun equals(o: Any?): Boolean {
        if (Tag::class.java.isAssignableFrom(o!!.javaClass)) {
            val otherTag = o as Tag?
            return this.id == otherTag!!.id
        }
        return super.equals(o)
    }
}
