package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.TutorialStep
import io.realm.RealmList


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Flags : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    internal actual var tutorial: RealmList<TutorialStep>? = null
    actual var showTour: Boolean = false
    actual var dropsEnabled: Boolean = false
    actual var itemsEnabled: Boolean = false
    actual var newStuff: Boolean = false
    actual var classSelected: Boolean = false
    actual var rebirthEnabled: Boolean = false
    actual var welcomed: Boolean = false
    actual var armoireEnabled: Boolean = false
    actual var armoireOpened: Boolean = false
    actual var armoireEmpty: Boolean = false
    actual var isCommunityGuidelinesAccepted: Boolean = false
    actual var isVerifiedUsername: Boolean = false
    actual var isWarnedLowHealth: Boolean = false
}
