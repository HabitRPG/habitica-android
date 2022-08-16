package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class UserTaskPreferences: RealmObject(), BaseObject {
    var confirmScoreNotes: Boolean = false
    var mirrorGroupTasks: RealmList<String> = RealmList()
    var groupByChallenge: Boolean = false
}
