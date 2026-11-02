package com.habitrpg.android.habitica.models.social

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
open class CategoryOption : RealmObject() {
    @PrimaryKey
    var key: String = ""
    var label: String = ""
}
