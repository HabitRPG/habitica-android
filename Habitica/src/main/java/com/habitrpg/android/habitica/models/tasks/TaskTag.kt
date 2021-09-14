package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.Tag
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class TaskTag : RealmObject(), BaseObject {
    var tag: Tag? = null
    var task: Task? = null
}
