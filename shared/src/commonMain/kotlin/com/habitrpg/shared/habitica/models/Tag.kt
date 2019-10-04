package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper

expect open class Tag {
    var id: String

    var userId: String?
    var tasks: RealmListWrapper<Task>?
    var name: String
    internal var challenge: Boolean

    fun getTasks(): List<Task>?

    override fun equals(o: Any?): Boolean
}