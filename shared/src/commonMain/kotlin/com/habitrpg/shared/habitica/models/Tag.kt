package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Tag : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var id: String = ""

    var userId: String? = null
    var tasks: NativeList<Task>? = null
    var name: String = ""
    internal var challenge: Boolean = false

    fun getTasks(): List<Task>? {
        return tasks
    }

    override fun equals(o: Any?): Boolean {
        if (o is Tag) {
            val otherTag = o as Tag?
            return this.id == otherTag!!.id
        }
        return super.equals(o)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

