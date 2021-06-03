package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class ChatMessageLike : RealmObject, BaseObject {

    @PrimaryKey
    var key: String = ""

    var messageId: String = ""
        set(value) {
            field = value
            key = messageId + id
        }
    var id: String = ""
        set(value) {
            field = value
            key = messageId + id
        }
    constructor(id: String, messageId: String) : super() {
        this.id = id
        this.key = messageId + id
    }

    constructor() : super()
}
