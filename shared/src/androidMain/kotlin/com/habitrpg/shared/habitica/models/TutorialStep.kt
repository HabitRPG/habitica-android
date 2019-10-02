package com.habitrpg.shared.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

actual open class TutorialStep : RealmObject() {

    @PrimaryKey
    actual var key: String? = null
    actual var tutorialGroup: String? = null
        set(group) {
            field = group
            this.key = group + "_" + this.identifier
        }
    actual var identifier: String? = null
        set(identifier) {
            field = identifier
            this.key = this.tutorialGroup + "_" + identifier
        }
    actual var wasCompleted: Boolean = false
    actual var displayedOn: Date? = null

    actual fun shouldDisplay(): Boolean =
            !this.wasCompleted && (this.displayedOn == null || Date().time - (displayedOn?.time ?: 0) > 86400000)
}
