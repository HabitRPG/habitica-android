package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Tag : RealmObject() {

    @PrimaryKey
    actual var id: String = ""

    actual var userId: String? = null
    actual var tasks: RealmListWrapper<Task>? = null
    actual var name: String = ""
    internal actual var challenge: Boolean = false

    actual fun getTasks(): List<Task>? {
        return tasks
    }


    actual override fun equals(o: Any?): Boolean {
        if (Tag::class.java.isAssignableFrom(o!!.javaClass)) {
            val otherTag = o as Tag?
            return this.id == otherTag!!.id
        }
        return super.equals(o)
    }
}
