package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.TutorialStep
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Flags : NativeRealmObject() {
    @PrimaryKeyAnnotation
    var userId: String? = null
    var tutorial: NativeList<TutorialStep>? = null
    var showTour = false
    var dropsEnabled = false
    var itemsEnabled = false
    var newStuff = false
    var classSelected = false
    var rebirthEnabled = false
    var welcomed = false
    var armoireEnabled = false
    var armoireOpened = false
    var armoireEmpty = false
    var communityGuidelinesAccepted = false
    var verifiedUsername = false
    var isWarnedLowHealth = false
}
