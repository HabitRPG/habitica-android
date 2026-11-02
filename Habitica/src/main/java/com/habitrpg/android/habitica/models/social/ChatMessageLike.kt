package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class ChatMessageLike(
    var id: String = "",
) : RealmObject(),
    BaseObject
