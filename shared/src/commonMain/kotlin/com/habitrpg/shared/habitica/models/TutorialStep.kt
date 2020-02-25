package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class TutorialStep : NativeRealmObject() {

    @PrimaryKeyAnnotation
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
    var displayedOn: NativeDate? = null

    fun shouldDisplay(): Boolean =
            !this.wasCompleted && (this.displayedOn == null || NativeDate().time - (displayedOn?.time ?: 0) > 86400000)
}
