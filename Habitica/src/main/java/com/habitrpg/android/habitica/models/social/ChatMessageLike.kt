package com.habitrpg.android.habitica.models.social

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by phillip on 30.06.17.
 */

open class ChatMessageLike : RealmObject {

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
