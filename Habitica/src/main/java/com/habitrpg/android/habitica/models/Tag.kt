package com.habitrpg.android.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Tag : RealmObject(), BaseObject {

    @PrimaryKey
    var id: String = ""

    var userId: String? = null
    var name: String = ""
    var group: String? = null
    internal var challenge: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other is Tag) {
            return this.id == other.id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
