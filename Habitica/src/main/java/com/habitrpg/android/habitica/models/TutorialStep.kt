package com.habitrpg.android.habitica.models

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date

@RealmClass(embedded = true)
open class TutorialStep : RealmObject(), BaseMainObject {

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

    val shouldDisplay: Boolean
        get() {
            return !this.wasCompleted && (
                this.displayedOn == null || Date().time - (
                    displayedOn?.time
                        ?: 0
                    ) > 86400000
                )
        }
    override val realmClass: Class<out RealmModel>
        get() = TutorialStep::class.java
    override val primaryIdentifier: String?
        get() = key
    override val primaryIdentifierName: String
        get() = "key"

    val flagPath: String
        get() = "flags.tutorial.$tutorialGroup.$identifier"

    val linkFAQ : Boolean
        get() {
            return identifier == "party"
        }
}
