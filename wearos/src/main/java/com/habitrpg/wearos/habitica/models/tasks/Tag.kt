package com.habitrpg.wearos.habitica.models.tasks

open class Tag {

    var id: String = ""

    var userId: String? = null
    var name: String = ""
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
