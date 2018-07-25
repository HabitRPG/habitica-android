package com.habitrpg.android.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class TutorialStep : RealmObject() {

    @PrimaryKey
    var key: String? = null
    var tutorialGroup: String? = null
        set(group) {
            field = group
            this.key = group + "_" + this.identifier
        }
    var identifier: String? = null
        set(identifier) {
            field = identifier
            this.key = this.tutorialGroup + "_" + identifier
        }
    var wasCompleted: Boolean = false
    var displayedOn: Date? = null

    fun shouldDisplay(): Boolean =
            !this.wasCompleted && (this.displayedOn == null || Date().time - (displayedOn?.time ?: 0) > 86400000)
}
