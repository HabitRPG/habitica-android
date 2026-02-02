package com.habitrpg.android.habitica.models.social

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ChallengeCategory(
    @PrimaryKey
    var id: String = "",
    var slug: String = "",
    var name: String = ""
) : RealmObject()