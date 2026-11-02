@file:UseSerializers(RealmListSerializer::class)

package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.RealmListSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@RealmClass(embedded = true)
@Serializable
open class UserTaskPreferences :
    RealmObject(),
    BaseObject {
    var confirmScoreNotes: Boolean = false
    var mirrorGroupTasks: RealmList<String> = RealmList()
    var groupByChallenge: Boolean = false
}
